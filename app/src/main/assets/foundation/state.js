var State = (() => {

	const holder = document.querySelector('body');

	let mode = 'normal';
	let shuffle = false;
	let repeat = RepeatMode.NO_REPEAT;
	let sort = Sort.AZ;

	let currentDir = '';
	let fileList = [];

	let currentTrack = '';
	let duration = '';
	let currentPosition = '';
	let album = '';
	let artist = '';
	let art;

	async function restore() {
		const rootDir = Prefs.read(StateKey.ROOT_DIR);
		if (rootDir) set(StateKey.ROOT_DIR, rootDir, true);

		const currentDir = Prefs.read(StateKey.CURRENT_DIR) || rootDir || await Native.FS.audioDir();
		set(StateKey.CURRENT_DIR, currentDir, true);
		set(StateKey.MODE, Prefs.read(StateKey.MODE), 'normal');
		set(StateKey.SEEK, Prefs.read(StateKey.SEEK), true);
		set(StateKey.SHUFFLE, Prefs.read(StateKey.SHUFFLE), true);
		set(StateKey.REPEAT, Prefs.read(StateKey.REPEAT), true);

		set(StateKey.PAUSED, true, true);
		set(StateKey.TRACK, Prefs.read(StateKey.TRACK), true);
		set(StateKey.DURATION, Prefs.read(StateKey.DURATION), true);
		set(StateKey.ALBUM, Prefs.read(StateKey.ALBUM) || quotes[Math.randomInt(0, 99)], true);
		set(StateKey.ARTIST, Prefs.read(StateKey.ARTIST), true);
	}

	function set(key, val, noSave) {
		holder.setAttribute(key, val);
		if (!noSave) Prefs.write(key, val);
	}

	function get(key) {
		return holder.getAttribute(key);
	}

	return {
		restore,
		set,
		get,
	}

})();
