var MockState = (() => {

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
