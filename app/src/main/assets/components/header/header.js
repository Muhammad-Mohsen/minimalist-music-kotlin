class MusicHeader extends HTMLElementBase {

	#TARGET = EventBus.Target.HEADER;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.handler(event));
	}

	// EVENT BUS
	handler(event) {
		if (event.target == this.#TARGET) return;

		when(event.type)
			.is([EventBus.Type.RESTORE_STATE, EventBus.Type.DIR_UPDATE], () => {
				this.style.opacity = 1;
				this.#renderCrumbs();
			})
			.is(EventBus.Type.SELECT_MODE_COUNT, () => this.selectCount.innerHTML = `${state.selection.length} selected`)
			.is(EventBus.Type.MODE_NORMAL, () => this.onCancelClick())
			.is(EventBus.Type.MODE_CHANGE, () => {
				if (state.mode == state.Mode.SEARCH) this.searchInput.focus();
			})
	}

	// UI HANDLERS
	onCrumbClick(crumb) {
		state.currentDir = crumb.getAttribute('path');
		this.#renderCrumbs();
		EventBus.dispatch({ type: EventBus.Type.DIR_CHANGE, target: this.#TARGET, data: { dir: state.currentDir } });
	}
	onBackClick() {
		state.currentDir = Path.join(state.currentDir.split(Path.SEPARATOR).slice(0, -1));
		this.#renderCrumbs();
		EventBus.dispatch({ type: EventBus.Type.DIR_CHANGE, target: this.#TARGET, data: { dir: state.currentDir } });
	}

	onCancelClick() {
		// reset the query + selection
		state.query = '';
		state.selection = [];
		this.searchInput.value = '';

		// change mode to crumbs
		state.mode = state.Mode.NORMAL;
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET, data: { mode: state.mode } });
	}
	onSearchInput(searchInput) {
		// update the filtering
		state.query = searchInput.value;
		EventBus.dispatch({ type: EventBus.Type.SEARCH_MODE, target: this.#TARGET });
	}
	onAddToQueueClick() {
		state.playlist.tracks = state.playlist.tracks.concat(state.selection);
		EventBus.dispatch({ type: EventBus.Type.QUEUE_ADD_SELECTED, target: this.#TARGET, data: { tracks: state.selection } });
		this.onCancelClick();
	}
	onPlaySelectedClick() {
		state.playlist.tracks = state.selection;
		EventBus.dispatch({ type: EventBus.Type.QUEUE_PLAY_SELECTED, target: this.#TARGET, data: { tracks: state.selection } });
		this.onCancelClick();
	}

	// RENDERING
	#render() {
		super.render(`
			<div id="breadcrumb-bar">
				<button id="back-button" onclick="${this.handle}.onBackClick()" class="ic-btn ic-arrow-left" aria-label="Back"></button>
				<ul id="crumbs"></ul>
			</div>

			<div id="toolbar">
				<button id="toolbar-cancel-button" class="ic-btn ic-arrow-left" onclick="${this.handle}.onCancelClick()" aria-label="Cancel"></button>
				<input id="search-input" type="search" placeholder="Search" oninput="${this.handle}.onSearchInput(this);">
				<span id="select-count"></span>
				<button id="select-add-button" class="ic-btn ic-add" onclick="${this.handle}.onAddToQueueClick()" aria-label="Add to Queue"></button>
				<button id="select-play-button" class="ic-btn ic-play-selected" onclick="${this.handle}.onPlaySelectedClick()" aria-label="Play Selection"></button>
			</div>
		`);
	}

	#renderCrumbs() {
		const dir = state.currentDir;

		// this.backButton.toggleAttribute('disabled', Path.eq(state.rootDir, dir));

		this.crumbs.innerHTML = '';
		dir.split(Path.SEPARATOR).reduce((path, seg) => {
			if (!seg) return path;

			path = path ? Path.join([path, seg]) : seg;

			this.crumbs.insertAdjacentHTML('beforeend',
				`<button path="${path}" onclick="${this.handle}.onCrumbClick(this)">${seg}</button>`);

			return path;

		}, '');

		this.crumbs.scrollTo(this.crumbs.scrollWidth, 0);
	}
}

customElements.define('music-header', MusicHeader);
