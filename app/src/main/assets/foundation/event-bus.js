var EventBus = (() => {

	// the event `type`
	const Type = {
		PERMISSION_REQUEST: 'permissionRequest',
		PERMISSION_RESPONSE: 'permissionResponse',

		INSETS: 'insets',
		RESTORE_STATE: 'restoreState',

		MODE_CHANGE: 'modeChange',
		MODE_NORMAL: 'modeNormal',

		SEARCH_MODE: 'search',

		PLAYLIST_UPDATE: 'playlistUpdate',
		QUEUE_PLAY_SELECTED: 'queuePlaySelected', // play the selected items (from breadcrumb bar)
		QUEUE_ADD_SELECTED: 'queueAddSelected',
		SELECT_MODE_COUNT: 'selectModeCount', // select mode count change
		SELECT_MODE_CANCEL: 'selectModeCancel', // deactivate select mode (press cancel from the breadcrumb bar)

		DIR_CHANGE: 'dirChange',
		DIR_UPDATE: 'dirUpdate',

		METADATA_UPDATE: 'metadataUpdate',
		PLAY_TRACK: 'playTrack',
		PLAY_NEXT: 'playNext',
		PLAY_PREV: 'playPrev',
		PLAY: 'play',
		PAUSE: 'pause',
		PLAY_PAUSE: 'playPause',
		FF: 'ff',
		RW: 'rw',
		SEEK_UPDATE: 'seekUpdate',
		SEEK_TICK: 'seekTick',

		SLEEP_TIMER_TOGGLE: 'sleepTimerToggle',
		SLEEP_TIMER_CHANGE: 'sleepTimerChange',
		SLEEP_TIMER_TICK: 'sleepTimerTick',
		SLEEP_TIMER_FINISH: 'sleepTimerFinish',

		THEME_CHANGE: 'themeChange',
		PLAYBACK_SPEED_CHANGE: 'playbackSpeedChange',
		SEEK_JUMP_CHANGE: 'seekJumpChange',
		SORT_BY_CHANGE: 'sortByChange',
		TOGGLE_SHUFFLE: 'toggleShuffle',
		TOGGLE_REPEAT: 'toggleRepeat',

		EQUALIZER_INFO: 'equalizerInfo',
		EQUALIZER_PRESET_CHANGE: 'equalizerPresetChange',
		EQUALIZER_BAND_CHANGE: 'equalizerBandChange',

		PRIVACY_POLICY: 'privacyPolicy',
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
		SETTINGS: 'settings',
		EQUALIZER: 'equalizer',
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
