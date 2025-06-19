class MusicHeader extends HTMLElementBase {

	#TARGET = EventBus.Target.HEADER;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.handler(event));
	}

	handler(event) {
		if (event.target == this.#TARGET) return;

		when(event.type)
			.is(EventBus.Type.RESTORE_STATE, () => this.#renderCrumbs())
			.is(EventBus.Type.DIR_CHANGE, () => this.#renderCrumbs())
			.is(EventBus.Type.SELECT_MODE_ADD, () => {})
			.is(EventBus.Type.SELECT_MODE_SUB, () => {})
			.is(EventBus.Type.SELECT_MODE_CANCEL, () => {})
			.is(EventBus.Type.SEARCH_MODE, () => {});
				/*
				toggleEditMode(State.isSelectModeActive || State.isSearchModeActive)
				toggleSelectMode()
				toggleSearchMode()
				*/
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
		EventBus.dispatch({ type: EventBus.Type.DIR_CHANGE, target: this.#TARGET, data: { directory: state.currentDir } });
	}

	onCancelClick() {
		// reset the query + selection
		state.query = '';
		state.selection = [];
		this.searchInput.value = '';

		// change mode to crumbs
		state.mode = state.Mode.NORMAL;
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET });
	}
	onSearchInput(searchInput) {
		// update the filtering
		state.query = searchInput.value;
		EventBus.dispatch({ type: EventBus.Type.SEARCH, target: this.#TARGET });
	}
	onAddToQueueClick() {
		EventBus.dispatch({ type: EventBus.Type.UPDATE_QUEUE, target: this.#TARGET });
	}
	onPlaySelectedClick() {
		EventBus.dispatch({ type: EventBus.Type.QUEUE_PLAY_SELECTED, target: this.#TARGET });

	}

	// RENDERING
	#render() {
		super.render(`
			<div id="breadcrumb-bar">
				<button id="back-button" onclick="${this.handle}.onBackClick()" class="ic-btn ic-arrow-left" aria-label="back"></button>
				<ul id="crumbs"></ul>
			</div>

			<div id="search-bar">
				<button id="search-cancel-button" class="ic-btn ic-arrow-left" aria-label="cancel" onclick="${this.handle}.onCancelClick()"></button>
				<input id="search-input" type="search" placeholder="Search" oninput="${this.handle}.onSearchInput(this);">
			</div>

			<div id="select-bar">
				<button id="select-cancel-button" class="ic-btn ic-arrow-left" aria-label="cancel"></button>
				<span id="select-count" l10n></span>
				<button id="select-add-button" class="ic-btn ic-play" aria-label="add to queue"></button>
				<button id="select-play-button" class="ic-btn ic-play" aria-label="play selection"></button>
			</div>
		`);
	}

	#renderCrumbs() {
		const dir = state.currentDir;

		this.backButton.toggleAttribute('disabled', Path.eq(state.rootDir, dir));

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
