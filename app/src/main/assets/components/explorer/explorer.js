class MusicExplorer extends HTMLElementBase {

	LONG_PRESS_THRESHOLD = 500;
	LONG_PRESS_MOVE_THRESHOLD = 10;

	Type = {
		DIR: 'dir',
		TRACK: 'track'
	}

	#TARGET = EventBus.Target.EXPLORER;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.#handler(event));
	}

	#handler(event) {
		if (event.target == this.#TARGET) return;

		when(event.type)
			.is([EventBus.Type.RESTORE_STATE, EventBus.Type.DIR_CHANGE], () => {
				this.#renderItems();
				this.#scrollToSelected();
			})
			.is([EventBus.Type.QUEUE_ADD_SELECTED, EventBus.Type.QUEUE_PLAY_SELECTED], () => {
				this.#updateItems();
			})
			.is(EventBus.Type.PLAY_TRACK, () => {
				const path = state.track.path;
				const target = document.querySelectorAll('.explorer.current button').toArray().find(f => f.getAttribute('path') == path);
				if (target) select(target);
			})
			.is(EventBus.Type.SEARCH, () => this.search())
			.is(EventBus.Type.MODE_CHANGE, () => {
				this.search(); // clear the search!
				if (![state.Mode.SELECT, state.Mode.SEARCH_SELECT].includes(state.mode)) this.#clearMarks(); // cancel selection if out of select mode
			})
	}

	// HANDLERS
	onItemTouchStart(event) {
		const target = event.currentTarget;
		if (target.getAttribute('type') == this.Type.DIR) return;

		const timeout = setTimeout(() => this.onItemLongTouch(target), this.LONG_PRESS_THRESHOLD);
		target.setAttribute('touch-start-ts', Date.now());
		target.setAttribute('timeout', timeout);

		target.setAttribute('touch-start-x', event.touches[0].clientX);
		target.setAttribute('touch-start-y', event.touches[0].clientY);
	}
	onItemTouchCancel(event) {
		clearTimeout(event.currentTarget.getAttribute('timeout'));
	}
	// cancel the long-press timeout if the user moves their finger
	onItemTouchMove(event) {
		const target = event.currentTarget;

		if (Math.abs(event.touches[0].clientX - parseFloat(target.getAttribute('touch-start-x'))) > this.LONG_PRESS_MOVE_THRESHOLD
				|| Math.abs(event.touches[0].clientY - parseFloat(target.getAttribute('touch-start-y'))) > this.LONG_PRESS_MOVE_THRESHOLD) {

			clearTimeout(target.getAttribute('timeout'));
		}
	}
	onItemTouchEnd(event) {
		const target = event.currentTarget;
		clearTimeout(target.getAttribute('timeout'));

		const path = target.getAttribute('path');
		const type = target.getAttribute('type');

		// DIRECTORY
		if (type == this.Type.DIR) {
			state.currentDir = path;
			return EventBus.dispatch({ type: EventBus.Type.DIR_CHANGE_REQUEST, target: this.#TARGET, data: { dir: state.currentDir } });
		}

		// LONG PRESS CHECK
		const touchStart = parseInt(target.getAttribute('touch-start-ts')) || Date.now();
		if (Date.now() - touchStart >= this.LONG_PRESS_THRESHOLD) return;

		// if already in select mode, do exactly as the longTouch
		if ([state.Mode.SELECT, state.Mode.SEARCH_SELECT].includes(state.mode)) return this.onItemLongTouch(target);

		// TRACK
		this.#select(target);
		EventBus.dispatch({ target: this.#TARGET, type: EventBus.Type.PLAY_TRACK_REQUEST, data: { track: target.getAttribute('path') } });
	}
	onItemLongTouch(target) {
		this.#mark(target);

		state.selection = this.querySelectorAll('.explorer.current .marked').toArray().map(i => i.getAttribute('path'));

		const toSelect = state.selection.length;
		if (toSelect) {
			state.mode = when(state.mode)
				.is([state.Mode.NORMAL, state.Mode.SELECT], () => state.Mode.SELECT)
				.is([state.Mode.SEARCH, state.Mode.SEARCH_SELECT], () => state.Mode.SEARCH_SELECT)
				.val();

		} else {
			state.mode = when(state.mode)
				.is(state.Mode.SELECT, () => state.Mode.NORMAL)
				.is(state.Mode.SEARCH_SELECT, () => state.Mode.SEARCH)
				.val();
		}

		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET });
		EventBus.dispatch({ type: EventBus.Type.SELECT_MODE_COUNT, target: this.#TARGET });
	}

	cancelSelectMode() {
		this.querySelectorAll('.explorer.current .marked').forEach(i => i.classList.remove('marked'));
		state.mode = state.Mode.NORMAL;
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET });
	}

	// SEARCH
	search() {
		var val = state.query;
		var explorer = document.querySelector('.explorer.current');

		var items = explorer.querySelectorAll('button > span').toArray();
		items.forEach(i => {
			const matches = i.textContent.fuzzyCompare(val);
			i.parentElement.classList.toggle('hidden', !matches);
			if (matches) i.innerHTML = this.#highlightSearchMatches(i, matches);
		});
	}

	//

	// RENDERING
	#render() {
		super.render(`
			<music-header id="header"></music-header>
			<div class="explorer-container">
				<ul class="explorer current" dir=""></ul>
				<ul class="explorer out" dir=""></ul>
				<ul class="explorer in" dir=""></ul>
			</div>
		`);
	}

	#renderItems() {
		const current = this.querySelector('.explorer.current');
		const outward = this.querySelector('.explorer.out');
		const inward = this.querySelector('.explorer.in');

		const previousDir = current.getAttribute('dir');
		const currentDir = state.currentDir;
		const toInward = currentDir.length > previousDir.length;

		const other = toInward ? inward : outward;
		const otherOther = toInward ? outward : inward;

		this.#updateItems(other);

		other.setAttribute('dir', currentDir);

		current.className = 'explorer ' + (toInward ? 'out' : 'in');
		other.className = 'explorer current';
		otherOther.className = 'explorer ' + (toInward ? 'in' : 'out');
	}
	#updateItems(explorer) {
		explorer ||= this.querySelector('.explorer.current');

		const files = state.files;
		explorer.innerHTML = '';
		files.forEach(file => explorer.insertAdjacentHTML('beforeend',
			`<button type="${file.type}" path="${file.path}"
					ontouchstart="${this.handle}.onItemTouchStart(event)" ontouchmove="${this.handle}.onItemTouchMove(event)" ontouchend="${this.handle}.onItemTouchEnd(event);" ontouchcanceled="${this.handle}.onItemTouchCancel(event);"
					class="${state.playlist.tracks.includes(file.path) ? 'playlist' : ''} ${Path.eq(state.track.path, file.path) ? 'selected' : ''}">

				<i class="selection"></i>
				<i class="${file.type == 'dir' ? 'ic-dir' : 'ic-music-note'}"></i>
				<span>${file.name}</span>
				<i class="ic-mark"></i>
			</button>`
		));
	}

	#select(target) {
		this.querySelector('.selected')?.classList?.remove('selected'); // deselect previous (if any)
		target.classList.add('selected');
	}
	#mark(target) {
		target.classList.toggle('marked');
	}
	#clearMarks() {
		this.querySelectorAll('.marked').forEach(i => i.classList.remove('marked'));
	}

	#highlightSearchMatches(element, matches) {
		let html = element.textContent;
		for (let i = matches.length - 1; i >= 0; i--) html = html.replaceAt(matches[i], `<b>${html[matches[i]]}</b>`);
		return html;
	}

	#scrollToSelected() {
		this.querySelector('.current .selected')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
	}
}

customElements.define('music-explorer', MusicExplorer);
