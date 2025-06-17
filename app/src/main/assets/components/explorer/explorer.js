class MusicExplorer extends HTMLElementBase {

	SELF = EventBus.Target.EXPLORER;

	connectedCallback() {
		this.#render();
		EventBus.subscribe(this.#handler);
	}

	#handler(event) {
		if (event.target == SELF) return;

		when(event.type)
			.is(EventBus.Type.DIR_CHANGE, () => update())
			.is(EventBus.Type.PLAY_TRACK, () => {
				const path = State.get(State.StateKey.TRACK);
				// const target = document.querySelector(`[path="${path.replace(/\\\\/g, '\\')}"]`); // doesn't work
				const target = document.querySelectorAll('explorer.current button').toArray().find(f => f.getAttribute('path') == path);
				if (target) select(target);
			})
			.is(EventBus.Type.SEARCH, () => {
				if (State.get(State.StateKey.EXPANDED) == 'true') toggleSearchMode(true);
			});
	}

	onItemClick(target) {
		const path = target.getAttribute('path');
		const type = target.getAttribute('type');

		if (isDir(target)) {
			goto(path);

		} else {
			select(target);
			State.set(State.StateKey.TRACK, target.getAttribute('path'));
			EventBus.dispatch({ target: EventBus.Target.EXPLORER, type: EventBus.Type.PLAY_TRACK });
		}
	}
	onItemLongClick() {

	}

	#render() {
		super.render(`
			<music-header id="header"></music-header>
			<ul id="explorer"></ul>
		`);
	}

	#renderItem(file) {
		return `<button path="${file.path}" ondblclick="${this.handle}.onItemClick(this);" class="c-fg-l ${State.get(State.StateKey.TRACK) == file.path ? 'selected' : ''}">
			<i class="material-symbols-outlined">${Native.FS.isDir(file) ? 'folder' : 'music_note'}</i>
			<span>${file.name}<span>
		</button>`;
	}
}

customElements.define('music-explorer', MusicExplorer);

// IMPORTS
const cache = new Map();

const searchContainer = document.querySelector('search-bar');
const searchInput = document.querySelector('search-bar input');

// EVENT BUS
EventBus.subscribe((event) => {
	if (event.target == SELF) return;

	when(event.type)
		.is(EventBus.Type.DIR_CHANGE, () => update())
		.is(EventBus.Type.PLAY_TRACK, () => {
			const path = State.get(State.StateKey.TRACK);
			// const target = document.querySelector(`[path="${path.replace(/\\\\/g, '\\')}"]`); // doesn't work
			const target = document.querySelectorAll('explorer.current button').toArray().find(f => f.getAttribute('path') == path);
			if (target) select(target);
		})
		.is(EventBus.Type.SEARCH, () => {
			if (State.get(State.StateKey.EXPANDED) == 'true') toggleSearchMode(true);
		});
});

// UI
async function update() {
	const files = await listFiles();

	const current = document.querySelector('explorer.current');
	const outward = document.querySelector('explorer.out');
	const inward = document.querySelector('explorer.in');

	const previousDir = current.getAttribute('data-dir');
	const currentDir = State.get(State.StateKey.CURRENT_DIR);
	const toInward = currentDir.length > previousDir.length;

	const other = toInward ? inward : outward;
	const otherOther = toInward ? outward : inward;

	// this is the actual important bit, everything else is just for the transition animation!
	other.innerHTML = '';
	files.forEach(f => other.insertAdjacentHTML('beforeend', createItem(f)));
	other.setAttribute('data-dir', currentDir);

	current.className = toInward ? 'out' : 'in';
	other.className = 'current';
	otherOther.className = toInward ? 'in' : 'out';
}

function createItem(file) {
	return `<button path="${file.path}" ondblclick="Explorer.onItemClick(this);" class="c-fg-l ${State.get(State.StateKey.TRACK) == file.path ? 'selected' : ''}">
			<i class="material-symbols-outlined">${Native.FS.isDir(file) ? 'folder' : 'music_note'}</i>
			<span>${file.name}<span>
		</button>`;
}

function select(target) {
	document.querySelector('explorer .selected')?.classList?.remove('selected'); // deselect previous (if any)
	target.classList.add('selected');
}

// CLICK HANDLERS
function onItemClick(target) {
	const path = target.getAttribute('path');

	if (isDir(target)) {
		goto(path);

	} else {
		select(target);
		State.set(State.StateKey.TRACK, target.getAttribute('path'));
		EventBus.dispatch({ target: EventBus.Target.EXPLORER, type: EventBus.Type.PLAY_TRACK });
	}
}

// SEARCH
function toggleSearchMode(force) {
	searchContainer.classList.toggle('show', force);
	searchInput.value = '';
	if (force) searchInput.focus();

	search();
}
function search() {
	var val = searchInput.value;
	var container = document.querySelector('explorer.current');

	var entries = container.querySelectorAll('button > span').toArray();
	entries.forEach(e => {
		const matches = e.textContent.fuzzyCompare(val);
		e.parentElement.classList.toggle('hidden', !matches);
		if (matches) e.innerHTML = highlightMatches(e, matches);
	});
}
function highlightMatches(element, matches) {
	let html = element.textContent;

	for (let i = matches.length - 1; i >= 0; i--) html = html.replaceAt(matches[i], `<b>${html[matches[i]]}</b>`);
	return html;
}

// SCROLL
async function scrollToSelected() {
	// navigate to selected dir
	const track = State.get(State.StateKey.TRACK);
	const dir = track.split(Native.FS.PATH_SEPARATOR).slice(0, -1).join(Native.FS.PATH_SEPARATOR);
	await goto(dir);

	document.querySelector('.selected')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// FS
function goto(dir) {
	const current = State.get(State.StateKey.CURRENT_DIR);
	if (dir == current) return;

	State.set(State.StateKey.CURRENT_DIR, dir);
	EventBus.dispatch({ target: SELF, type: EventBus.Type.DIR_CHANGE });
	return update();
}
async function listFiles() {
	const current = State.get(State.StateKey.CURRENT_DIR);
	let files = cache.get(current);

	if (!files) {
		files = await Native.FS.listFiles(current);
		cache.set(current, files);
	}

	return files;
}
async function listTracks() {
	const files = await listFiles();
	return files.filter(f => Native.FS.isAudio(f)).map(f => f.path);
}
function isAtRoot() {
	return State.get(State.StateKey.CURRENT_DIR).length <= State.get(State.StateKey.ROOT_DIR).length;
}
function isDir(target) {
	return target.querySelector('i').innerHTML == 'folder';
}
