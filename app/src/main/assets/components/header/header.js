class MusicHeader extends HTMLElementBase {

	SELF = EventBus.Target.HEADER;

	connectedCallback() {
		this.#render();
		EventBus.subscribe(this.#handler);
	}

	#handler(event) {

	}

	onCrumbClick() {
		// last item shouldn't be clickable

		// update the state with the new directory

		// re-render the crumbs

		// dispatch DIR_CHANGE
	}

	onCancelClick() {
		// change mode to crumbs

		// reset the selection

		// reset the filtering
	}
	onSearchInput() {
		// update the filtering
	}
	onAddToQueueClick() {
		// change mode back to crumbs

		// get items from explorer

		// dispatch UPDATE_QUEUE with items
	}
	onPlaySelectedClick() {
		// change mode back to crumbs

		// get items from explorer

		// dispatch UPDATE_QUEUE with items (should exactly be the same as onAddToQueue except that, expectedly, it won't add)
	}

	#render() {
		super.render(`
			<div id="breadcrumb-bar">
				<button id="back-button" class="ic-btn ic-arrow-left pressable" aria-label="back" l10n></button>
				<ul id="crumbs"></ul>
			</div>

			<div id="search-bar">
				<button id="search-cancel-button" class="ic-btn ic-arrow-left pressable" aria-label="cancel" l10n></button>
				<input id="search-input" type="search">
			</div>

			<div id="select-bar">
				<button id="select-cancel-button" class="ic-btn ic-arrow-left pressable" aria-label="cancel" l10n></button>
				<span id="select-count" l10n></span>
				<button id="select-add-button" class="ic-btn ic-play" aria-label="add to queue" l10n></button>
				<button id="select-play-button" class="ic-btn ic-play" aria-label="play selection" l10n></button>
			</div>
		`);
	}

	#renderCrumbs() {

	}
}

customElements.define('music-header', MusicHeader);


// IMPORTS
const SELF = EventBus.Target.HEADER;

// const container = document.querySelector('breadcrumb-bar');
// const crumbs = container.querySelector('crumb-list');

// EVENT BUS
EventBus.subscribe((event) => {
	if (event.target == SELF) return;

	/*
	when (data.type) {
		EventBus.Type.DIR_CHANGE -> onDirectoryChange(State.currentDirectory)

		EventBus.Type.SELECT_MODE_ADD,
		EventBus.Type.SELECT_MODE_SUB,
		EventBus.Type.SELECT_MODE_INACTIVE,
		EventBus.Type.SEARCH_MODE -> {
			toggleEditMode(State.isSelectModeActive || State.isSearchModeActive)
			toggleSelectMode()
			toggleSearchMode()
		}
	}
	*/

	when(event.type)
		.is(EventBus.Type.DIR_CHANGE, () => update());
});

function update() {
	const current = State.get(State.Key.CURRENT_DIR);

	crumbs.innerHTML = '';
	current.split(Native.FS.PATH_SEPARATOR).reduce((acc, curr) => {
		if (!curr) return acc;

		const pathSegment = acc ? acc + Native.FS.PATH_SEPARATOR + curr : curr;

		crumbs.insertAdjacentHTML('beforeend', crumb(pathSegment));

		return pathSegment;

	}, '');

	crumbs.scrollTo(crumbs.scrollWidth, 0);
}

function up() {
	if (Explorer.isAtRoot()) return; // don't go back beyond the root

	const current = State.get(State.StateKey.CURRENT_DIR);

	const segments = current.split(Native.FS.PATH_SEPARATOR);
	segments.pop();
	const dir = segments.join(Native.FS.PATH_SEPARATOR);

	State.set(State.StateKey.CURRENT_DIR, dir);
	EventBus.dispatch({ target: SELF, type: EventBus.Type.DIR_CHANGE });
	update();
}

function crumb(path) {
	const label = path.split(Native.FS.PATH_SEPARATOR).pop();
	return `<button path="${path}" onclick="BreadcrumbBar.onCrumbClick(this)" class="c-fg-l">${label}</button>`;
}

function onCrumbClick(target) {
	const dir = target.getAttribute('path');

	State.set(State.StateKey.CURRENT_DIR, dir);
	EventBus.dispatch({ target: SELF, type: EventBus.Type.DIR_CHANGE });
	update();
}
