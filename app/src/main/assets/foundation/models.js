const Mode = {
	PERMISSION: 'permission',
	NORMAL: 'normal',
	SEARCH: 'search',
	SELECT: 'select',
	LYRICS: 'dialoglyrics',
	CHAPTERS: 'dialogchapters',
	SETTINGS: 'dialogsettings',
}

const RepeatMode = {
	NO_REPEAT: 0,
	REPEAT: 1,
	REPEAT_ONE: 2,
}

const Sort = {
	AZ: 'az',
	ZA: 'za',
	NEWEST: 'newest',
	OLDEST: 'oldest'
}

const StateKey = {
	MODE: 'state_mode',
	CURRENT_DIR: 'state_current_dir',
	SORT: 'state_sort',
	SHUFFLE: 'state_shuffle',
	REPEAT: 'state_repeat',

	TRACK: 'state_track',
	PAUSED: 'state_paused',
	SEEK: 'state_seek',
	DURATION: 'state_duration',
	ALBUM: 'state_album',
	ARTIST: 'state_artist'
}

const ExplorerItemType = {
	DIRECTORY: 'directory',
	AUDIO: 'audio'
}
