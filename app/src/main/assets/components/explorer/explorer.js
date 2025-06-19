class MusicExplorer extends HTMLElementBase {

	SELF = EventBus.Target.EXPLORER;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.#handler(event));
	}

	#handler(event) {
		if (event.target == this.SELF) return;

		when(event.type)
			.is(EventBus.Type.DIR_CHANGE, () => this.#renderItems())
			.is(EventBus.Type.RESTORE_STATE, () => this.#renderItems())
			.is(EventBus.Type.PLAY_TRACK, () => {
				const path = State.get(State.Key.TRACK);
				// const target = document.querySelector(`[path="${path.replace(/\\\\/g, '\\')}"]`); // doesn't work
				const target = document.querySelectorAll('explorer.current button').toArray().find(f => f.getAttribute('path') == path);
				if (target) select(target);
			})
			.is(EventBus.Type.SEARCH, () => {
				if (State.get(State.Key.EXPANDED) == 'true') toggleSearchMode(true);
			});
	}

	onItemClick(target) {
		const path = target.getAttribute('path');
		const type = target.getAttribute('type');

		if (isDir(target)) {
			goto(path);

		} else {
			select(target);
			State.set(State.Key.TRACK, target.getAttribute('path'));
			EventBus.dispatch({ target: EventBus.Target.EXPLORER, type: EventBus.Type.PLAY_TRACK });
		}
	}
	onItemLongClick() {

	}

	#render() {
		super.render(`
			<music-header id="header"></music-header>
			<div class="explorer-container">
				<ul class="explorer current" directory=""></ul>
				<ul class="explorer out" directory=""></ul>
				<ul class="explorer in" directory=""></ul>
			</div>
		`);
	}

	#renderItems() {
		const files = state.files;

		const current = this.querySelector('.explorer.current');
		const outward = this.querySelector('.explorer.out');
		const inward = this.querySelector('.explorer.in');

		const previousDir = current.getAttribute('directory');
		const currentDir = state.currentDir;
		const toInward = currentDir.length > previousDir.length;

		const other = toInward ? inward : outward;
		const otherOther = toInward ? outward : inward;

		// this is the actual important bit, everything else is just for the transition animation!
		other.innerHTML = '';
		files.forEach(file => other.insertAdjacentHTML('beforeend',
			`<button path="${file.path}" onclick="${this.handle}.onItemClick(this);"
					class="${file.type} ${state.playlist.tracks.includes(file.path) ? 'playlist' : ''} ${Path.eq(state.track.path, file.path) ? 'selected' : ''}">

				<i class="${file.type == 'directory' ? 'ic-directory' : 'ic-music-note'}"></i>
				<span>${file.name}<span>
			</button>`
		));

		other.setAttribute('directory', currentDir);

		current.className = 'explorer ' + (toInward ? 'out' : 'in');
		other.className = 'explorer current';
		otherOther.className = 'explorer ' + (toInward ? 'in' : 'out');
	}
}

customElements.define('music-explorer', MusicExplorer);

// IMPORTS
const cache = new Map();

const searchContainer = document.querySelector('search-bar');
const searchInput = document.querySelector('search-bar input');

// UI
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
		State.set(State.Key.TRACK, target.getAttribute('path'));
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
	const track = State.get(State.Key.TRACK);
	const dir = track.split(Native.FS.PATH_SEPARATOR).slice(0, -1).join(Native.FS.PATH_SEPARATOR);
	await goto(dir);

	document.querySelector('.selected')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// FS
function goto(dir) {
	const current = State.get(State.Key.CURRENT_DIR);
	if (dir == current) return;

	State.set(State.Key.CURRENT_DIR, dir);
	EventBus.dispatch({ target: SELF, type: EventBus.Type.DIR_CHANGE });
	return update();
}
async function listFiles() {
	const current = State.get(State.Key.CURRENT_DIR);
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
	return State.get(State.Key.CURRENT_DIR).length <= State.get(State.Key.ROOT_DIR).length;
}
function isDir(target) {
	return target.querySelector('i').innerHTML == 'folder';
}
