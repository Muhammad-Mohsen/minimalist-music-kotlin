class State {

	Mode = {
		PERMISSION: 'permission',
		NORMAL: 'normal',
		SELECT: 'select',
		SEARCH: 'search',
		SEARCH_SELECT: 'searchSelect',
		SETTINGS: 'settings',
	}

	Theme = {
		LIGHT: 'light',
		DARK: 'dark'
	}

	get mode() { return document.body.getAttribute('mode') || this.Mode.NORMAL; }
	set mode(val) { document.body.setAttribute('mode', val); }

	currentDir = '';
	files = []; // { type, path, name }
	get currentDirName() { return this.currentDir.split(Path.SEPARATOR).pop() }

	get isPlaying() { return document.body.getAttribute('is-playing') == 'true' }
	set isPlaying(val) { document.body.setAttribute('is-playing', val) }

	selection = [];
	query = '';

	settings = {
		get theme() { return document.body.getAttribute('theme') || this.Theme.DARK; },
		set theme(val) { document.body.setAttribute('theme', val); },

		shuffle: false,
		repeat: RepeatMode.NO_REPEAT,
		sortBy: SortBy.AZ,

		playbackSpeed: 1,
		sleepTimer: '',
		seekJump: 30,
	}

	playlist = {
		tracks: [],
		index: 0,
	}

	track = {
		path: '',
		name: '',
		duration: '',
		seek: '',
		album: '',
		artist: '',
		albumArt: '',
		chapters: [],
		lyrics: '',
	}

	restore(serializedState) {
		this.currentDir = serializedState.currentDir;
		this.files = serializedState.files;
		this.isPlaying = false;

		this.updateSettings(serializedState.settings);
		this.updatePlaylist(serializedState.playlist);
		this.updateTrack(serializedState.track);
	}
	updateSettings(settings) {
		this.settings.theme = settings.theme;

		this.settings.shuffle = settings.shuffle;
		this.settings.repeat = settings.repeat;
		this.settings.sortBy = settings.sortBy;

		this.settings.playbackSpeed = settings.playbackSpeed;
		this.settings.sleepTimer = settings.sleepTimer;
		this.settings.seekJump = settings.seekJump;
	}
	updateTrack(track) {
		this.track.path = track.path;
		this.track.name = track.name;
		this.track.duration = track.duration;
		this.track.seek = track.seek;
		this.track.album = track.album;
		this.track.artist = track.artist;
		this.track.albumArt = track.albumArt;
		this.track.chapters = track.chapters;

		if (!track.name) {
			this.track.name = 'Hi,',
			this.track.album = 'welcome to Minimalist Music'
		}
	}
	updatePlaylist(playlist) {
		this.playlist.tracks = playlist.tracks;
		this.playlist.index = playlist.index;
	}
}
