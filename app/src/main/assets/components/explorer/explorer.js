class MusicExplorer extends HTMLElementBase {

	LONG_PRESS_THRESHOLD = 600;
	LONG_PRESS_MOVE_THRESHOLD = 10;
	SAFE_AREA_TOUCH_THRESHOLD = 20;

	Type = {
		DIR: 'dir',
		TRACK: 'track'
	}

	#TARGET = EventBus.Target.EXPLORER;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.#handler(event));
	}

	// EVENT BUS
	#handler(event) {
		if (event.target == this.#TARGET) return;

		when(event.type)
			.is([EventBus.Type.RESTORE_STATE, EventBus.Type.DIR_UPDATE], () => {
				this.style.opacity = 1;

				this.#renderExplorer();
				this.#scrollToSelected();
			})
			.is([EventBus.Type.PLAYLIST_UPDATE, EventBus.Type.QUEUE_ADD_SELECTED, EventBus.Type.QUEUE_PLAY_SELECTED], () => {
				this.#updateItems();
			})
			.is([EventBus.Type.PLAY_TRACK, EventBus.Type.METADATA_UPDATE], () => {
				const path = state.track.path;
				const target = document.querySelectorAll('.explorer.current button').toArray().find(f => f.getAttribute('path') == path);
				if (target) this.#select(target);
			})
			.is(EventBus.Type.SEARCH_MODE, () => this.search())
			.is(EventBus.Type.MODE_CHANGE, () => {
				this.search(); // clear the search!
				if (![state.Mode.SELECT, state.Mode.SEARCH_SELECT].includes(state.mode)) this.#clearMarks(); // cancel selection if out of select mode
			})
	}

	// HANDLERS
	onItemTouchStart(event) {
		const target = event.currentTarget;

		// ignore touches in the near the edges -- sometimes, the long-press handler incorrectly fired during the back gesture
		const touchX = event.touches[0].clientX;
		if (touchX <= this.SAFE_AREA_TOUCH_THRESHOLD || touchX >= (target.clientWidth - this.SAFE_AREA_TOUCH_THRESHOLD)) return;

		// for scrolling
		target.setAttribute('touch-start-x', event.touches[0].clientX);
		target.setAttribute('touch-start-y', event.touches[0].clientY);

		if (target.getAttribute('type') == this.Type.DIR) return;

		// for long press
		const timeout = setTimeout(() => this.onItemLongTouch(target), this.LONG_PRESS_THRESHOLD);
		target.setAttribute('touch-start-ts', Date.now());
		target.setAttribute('timeout', timeout);
	}
	onItemTouchCancel(event) {
		clearTimeout(event.currentTarget.getAttribute('timeout'));
	}
	// cancel the long-press timeout if the user moves their finger
	onItemTouchMove(event) {
		const target = event.currentTarget;

		if (Math.abs(event.touches[0].clientX - parseFloat(target.getAttribute('touch-start-x'))) > this.LONG_PRESS_MOVE_THRESHOLD
		|| Math.abs(event.touches[0].clientY - parseFloat(target.getAttribute('touch-start-y'))) > this.LONG_PRESS_MOVE_THRESHOLD) {

			target.setAttribute('moved', ''); // mark the touch event as moved (to cancel it on touchup)
			clearTimeout(target.getAttribute('timeout'));
		}
	}
	onItemTouchEnd(event) {
		const target = event.currentTarget;
		clearTimeout(target.getAttribute('timeout'));

		if (target.hasAttribute('moved')) return target.removeAttribute('moved');

		const path = target.getAttribute('path');
		const type = target.getAttribute('type');

		// DIRECTORY
		if (type == this.Type.DIR) {
			state.currentDir = path;
			return EventBus.dispatch({ type: EventBus.Type.DIR_CHANGE, target: this.#TARGET, data: { dir: state.currentDir } });
		}

		// LONG PRESS (already handled)
		const touchStart = parseInt(target.getAttribute('touch-start-ts')) || Date.now();
		if (Date.now() - touchStart >= this.LONG_PRESS_THRESHOLD) return;

		// if already in select mode, do exactly as the longTouch
		if ([state.Mode.SELECT, state.Mode.SEARCH_SELECT].includes(state.mode)) return this.onItemLongTouch(target);

		// TRACK
		this.#select(target);
		EventBus.dispatch({ target: this.#TARGET, type: EventBus.Type.PLAY_TRACK, data: { path: target.getAttribute('path') } });
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
				.is([state.Mode.SELECT, state.Mode.NORMAL], () => state.Mode.NORMAL)
				.is(state.Mode.SEARCH_SELECT, () => state.Mode.SEARCH)
				.val();
		}

		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET, data: { mode: state.mode } });
		EventBus.dispatch({ type: EventBus.Type.SELECT_MODE_COUNT, target: this.#TARGET });
	}

	cancelSelectMode() {
		this.querySelectorAll('.explorer.current .marked').forEach(i => i.classList.remove('marked'));
		state.mode = state.Mode.NORMAL;
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET, data: { mode: state.mode } });
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

	// RENDERING
	#render() {
		super.render(`
			<div class="explorer-container">
				<ul class="explorer current" dir=""></ul>
				<ul class="explorer out" dir=""></ul>
				<ul class="explorer in" dir=""></ul>
			</div>
		`);
	}

	#renderExplorer() {
		const current = this.querySelector('.explorer.current');
		const outward = this.querySelector('.explorer.out');
		const inward = this.querySelector('.explorer.in');

		const previousDir = current.getAttribute('dir');
		const currentDir = state.currentDir;
		const toInward = currentDir.length > previousDir.length;

		const other = toInward ? inward : outward;
		const otherOther = toInward ? outward : inward;

		this.#renderItems(other);

		other.setAttribute('dir', currentDir);

		current.className = 'explorer ' + (toInward ? 'out' : 'in');
		other.className = 'explorer current';
		otherOther.className = 'explorer ' + (toInward ? 'in' : 'out');
	}
	#renderItems(explorer) {
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
	#updateItems() {
		this.querySelectorAll('.explorer.current [type="track"]').forEach(t => {
			const path = t.getAttribute('path');
			t.className = `${state.playlist.tracks.includes(path) ? 'playlist' : ''} ${Path.eq(state.track.path, path) ? 'selected' : ''}`
		});
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
		this.querySelector('.current .selected')?.scrollIntoView({ block: 'center' });
	}
}

customElements.define('music-explorer', MusicExplorer);
