class MockState {

	Key = {
		MODE: 'mode',
		CURRENT_DIR: 'currentDir',
		IS_PLAYING: 'isPlaying',
		SELECTION: 'selection',

		SETTINGS: 'settings',

		THEME: 'settings.theme',
		SHUFFLE: 'settings.shuffle',
		REPEAT: 'settings.repeat',
		SORT: 'settings.sort',

		PLAYBACK_SPEED: 'settings.playbackSpeed',
		SLEEP_TIMER: 'settings.sleepTimer',
		SEEK_JUMP: 'settings.seekJump',

		PLAYLIST: 'playlist',
		TRACKS: 'playlist.tracks',
		INDEX: 'playlist.index',

		TRACK: 'track',
		PATH: 'track.path',
		DURATION: 'track.duration',
		SEEK: 'track.seek',
		ARTIST: 'track.artist',
		ART: 'track.art',
		CHAPTERS: 'track.chapters',
	}

	Mode = {
		PERMISSION: 'permission',
		NORMAL: 'normal',
		SELECT: 'select',
		SEARCH: 'search',
	}

	mode = this.Mode.NORMAL;
	rootDir = 'D:\\Music + Audiobooks\\';
	currentDir = 'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3';
	files = [
		{
			type: 'directory',
			path: 'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\album art',
			name: 'album art',
		},

		{
			type: 'track',
			path: 'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\01 engage.mp3',
			name: '01 engage.mp3',
		},
		{
			type: 'track',
			path: 'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\02 linkage.mp3',
			name: '02 linkage.mp3',
		},
		{
			type: 'track',
			path: 'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\03 armory.mp3',
			name: '03 armory.mp3',
		},
		{
			type: 'track',
			path: 'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\04 transparent blue.mp3',
			name: '04 transparent blue.mp3',
		},
		{
			type: 'track',
			path: 'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\05 eye of the storm.mp3',
			name: '05 eye of the storm.mp3',
		},
	];

	isPlaying = false;
	selection = [];

	settings = {
		theme: 'dark',

		shuffle: false,
		repeat: RepeatMode.NO_REPEAT,
		sort: Sort.AZ,

		playbackSpeed: 1,
		sleepTimer: '',
		seekJump: 30,
	}

	playlist = {
		tracks: [
			'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\01 engage.mp3',
			'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\02 linkage.mp3',
			'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\03 armory.mp3',
		],
		index: 0,
	}

	track = {
		path: 'D:\\Music + Audiobooks\\MISC\\Games\\Ace Combat 3\\01 engage.mp3',
		name: '01 engage.mp3',
		duration: 186,
		seek: 5,
		album: 'Ace Combat 3',
		artist: 'Tetsukazu Nakanishi',
		art: '',
		chapters: [],
	}

	async restore() {
		const rootDir = Prefs.read(State.Key.ROOT_DIR);
		if (rootDir) set(State.Key.ROOT_DIR, rootDir, true);

		const currentDir = Prefs.read(State.Key.CURRENT_DIR) || rootDir || await Native.FS.audioDir();
		set(State.Key.CURRENT_DIR, currentDir, true);
		set(State.Key.MODE, Prefs.read(State.Key.MODE), 'normal');
		set(State.Key.SEEK, Prefs.read(State.Key.SEEK), true);
		set(State.Key.SHUFFLE, Prefs.read(State.Key.SHUFFLE), true);
		set(State.Key.REPEAT, Prefs.read(State.Key.REPEAT), true);

		set(State.Key.PAUSED, true, true);
		set(State.Key.TRACK, Prefs.read(State.Key.TRACK), true);
		set(State.Key.DURATION, Prefs.read(State.Key.DURATION), true);
		set(State.Key.ALBUM, Prefs.read(State.Key.ALBUM) || quotes[Math.randomInt(0, 99)], true);
		set(State.Key.ARTIST, Prefs.read(State.Key.ARTIST), true);
	}

	set(key, val, noSave) {
		holder.setAttribute(key, val);
		if (!noSave) Prefs.write(key, val);
	}

	get(key) {
		return holder.getAttribute(key);
	}
}

setTimeout(() => EventBus.dispatch({ type: EventBus.Type.RESTORE_STATE, target: 'MOCK' }), 300);