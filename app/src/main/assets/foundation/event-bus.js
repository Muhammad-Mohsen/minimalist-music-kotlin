var EventBus = (() => {

	// the event `type`
	const Type = {
		PERMISSION_REQUEST: 'permissionRequest',
		PERMISSION_RESPONSE: 'permissionResponse',

		DIR_CHANGE_REQUEST: 'dirChangeRequest',
		DIR_CHANGE: 'dirChange',
		MODE_CHANGE: 'modeChange',
		MODE_NORMAL: 'modeNormal',

		INSETS: 'insets',
		STATE_UPDATE_REQUEST: 'stateUpdateRequest',
		RESTORE_STATE: 'restoreState',

		TOGGLE_SHUFFLE: 'toggleShuffle',
		TOGGLE_REPEAT: 'toggleRepeat',

		PLAY_TRACK_REQUEST: 'playTrackRequest',
		PLAY_TRACK: 'playTrack',
		PLAY_NEXT_REQUEST: 'playNextRequest',
		PLAY_NEXT: 'playNext',
		PLAY_PREV_REQUEST: 'playPrevRequest',
		PLAY_PREV: 'playPrev',
		PLAY: 'play',
		PAUSE: 'pause',
		PLAY_PAUSE: 'playPause',
		SEEK_UPDATE: 'seekUpdate',
		SEEKING: 'seeking',
		FF: 'ff',
		RW: 'rw',

		METADATA_UPDATE: 'metadataUpdate',
		METADATA_FETCH: 'metadataFetch',

		SEARCH: 'search',

		QUEUE_PLAY_SELECTED: 'queuePlaySelected', // play the selected items (from breadcrumb bar)
		QUEUE_ADD_SELECTED: 'queueAddSelected',
		SELECT_MODE_COUNT: 'selectModeCount', // select mode count change
		SELECT_MODE_CANCEL: 'selectModeCancel', // deactivate select mode (press cancel from the breadcrumb bar)
	}

	// the event `target` (read: source)
	const Target = {
		EXPLORER: 'explorer',
		HEADER: 'header',
		CONTROLS: 'controls',
		MAIN: 'main',
		SESSION: 'session',
		PERMISSION_UI: 'permission',
		STATE: 'state',
	}

	const subscribers = []; // a regular ol' array will do

	function subscribe(sub) { subscribers.push(sub); }
	function unsubscribe(sub) { subscribers = subscribers.filter(s => s != sub); } // never gonna happen

	/**
	 * @param {{ type: EventBus.type, target: EventBus.target, data?: any }} event - The event object.
	 */
	function dispatch(event, native) {
		if (native) console.log(JSON.stringify(event));

		subscribers.forEach(callback => callback(event, native));
		if (!native) window.IPC?.dispatch(JSON.stringify(event));
	}

	return {
		Type,
		Target,

		subscribe,
		unsubscribe,

		dispatch,
	}

})();
