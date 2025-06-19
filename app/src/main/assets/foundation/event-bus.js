var EventBus = (() => {

	// the event `type`
	const Type = {
    		PERMISSION_REQUEST: 'permissionRequest',
			PERMISSION_SETTINGS: 'permissionSettings',
    		RESTORE_STATE: 'restoreState',
    		DIR_CHANGE: 'dirChange',
			INSETS: 'insets',

    		PLAY_TRACK: 'playTrack',
    		PLAY_NEXT: 'playNext',
    		PLAY_PREVIOUS: 'playPrev',
    		PLAY: 'play',
    		PAUSE: 'pause',
    		SEEK_UPDATE: 'seekUpdate',
    		VOLUME: 'volume',
    		METADATA_UPDATE: 'metadataUpdate',
    		METADATA_FETCH: 'metadataFetch',
    		METADATA_CLEAR: 'metadataClear',

    		TOGGLE_SHUFFLE: 'toggleShuffle',
    		TOGGLE_REPEAT: 'toggleRepeat',

    		FF: 'ff',
    		RW: 'rw',
    		PLAY_PAUSE: 'playPause',

    		SEARCH: 'search',

    		PLAY_SELECTED: 'playSelected', // play the selected items (from breadcrumb bar)
    		SELECT_MODE_ADD: 'selectModeAdd', // add a track to the selected list (activate the mode if none were selected before)
    		SELECT_MODE_SUB: 'selectModeSub', // remove a track from the selected list (deactivate the mode if none are selected now)
    		SELECT_MODE_CANCEL: 'selectModeCancel', // deactivate select mode (press cancel from the breadcrumb bar)
    		SEARCH_MODE: 'searchMode',
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
