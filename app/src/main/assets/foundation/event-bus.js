var EventBus = (() => {

	// the event `type`
	const Type = {
    		PERMISSION_REQUEST: 'permissionRequest',
			PERMISSION_SETTINGS_REQUEST: 'permissionSettingsRequest',
    		RESTORE_STATE: 'restoreState',
    		DIR_CHANGE_REQUEST: 'dirChangeRequest',
    		DIR_CHANGE: 'dirChange',
			INSETS: 'insets',

			MODE_CHANGE: 'modeChange',

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
    		FF: 'ff',
    		RW: 'rw',

			METADATA_UPDATE: 'metadataUpdate',
    		METADATA_FETCH: 'metadataFetch',

    		TOGGLE_SHUFFLE: 'toggleShuffle',
    		TOGGLE_REPEAT: 'toggleRepeat',

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
		PERMISSION_UI: 'permission'
	}

	const subscribers = []; // a regular ol' array will do

	function subscribe(sub) { subscribers.push(sub); }
	function unsubscribe(sub) { subscribers = subscribers.filter(s => s != sub); } // never gonna happen

	/**
	 * @param {{ type: EventBus.type, target: EventBus.target, data?: any }} event - The event object.
	 */
	function dispatch(event, fromNative) {
		subscribers.forEach(callback => callback(event));
		if (!fromNative) window.IPC?.dispatch(JSON.stringify(event));

		if (fromNative) console.log(event);
	}

	return {
		Type,
		Target,

		subscribe,
		unsubscribe,

		dispatch,
	}

})();
