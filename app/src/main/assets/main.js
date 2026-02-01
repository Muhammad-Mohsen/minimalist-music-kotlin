if (location.href.includes('mode=permission')) document.body.setAttribute('mode', 'permission'); // permission layout

const state = new State();

EventBus.subscribe((event, native) => {
	if (!native) return; // this should only handle native events basically to sync the state

	when(event.type)
		.is(EventBus.Type.INSETS, () => {
			document.body.style.setProperty('--inset-top', event.data.top / devicePixelRatio + 'px');
			document.body.style.setProperty('--inset-bottom', event.data.bottom / devicePixelRatio + 'px');
		})
		.is(EventBus.Type.PERMISSION_RESPONSE, () => {
			// current state is PERMISSION && response is GRANTED => show normal UI
			if (event.data.mode == state.Mode.NORMAL && state.mode == state.Mode.PERMISSION) state.mode = state.Mode.NORMAL;
			// current state isn't PERMISSION && response is REVOKED => show permission UI
			else if (event.data.mode == state.Mode.PERMISSION && state.mode != state.Mode.PERMISSION) state.mode = state.Mode.PERMISSION;
		})
		.is(EventBus.Type.RESTORE_STATE, () => state.restore(event.data))
		.is(EventBus.Type.DIR_UPDATE, () => {
			if (event.data.currentDir) state.currentDir = event.data.currentDir;
			state.files = event.data.files;
		})
		.is(EventBus.Type.PLAYLIST_UPDATE, () => state.updatePlaylist(event.data))
		.is(EventBus.Type.METADATA_UPDATE, () => state.updateTrack(event.data))
});

console.log('JUST TO BE SPORTING')