addEventListener("DOMContentLoaded", () => {
	if (location.href.includes('mode=permission')) document.body.setAttribute('mode', 'permission'); // permission layout
	setTimeout(() => document.body.classList.add('ready')); // reveal animation
});

const state = new State();

EventBus.subscribe((event, native) => {
	if (!native) return; // this should only handle native events basically to sync the state

	when(event.type)
		.is(EventBus.Type.INSETS, () => {
			document.body.style = `--inset-top: ${event.data.top / devicePixelRatio}px; --inset-bottom: ${event.data.bottom / devicePixelRatio}px;`;
		})
		.is(EventBus.Type.PERMISSION_RESPONSE, () => {
			// current state is PERMISSION && response is GRANTED => show normal UI
			if (event.data.mode == state.Mode.NORMAL && state.mode == state.Mode.PERMISSION) state.mode = state.Mode.NORMAL;
			// current state isn't PERMISSION && response is REVOKED =>
			else if (event.data.mode == state.Mode.PERMISSION && state.mode != state.Mode.PERMISSION) state.mode = state.Mode.PERMISSION;
		})
		.is(EventBus.Type.RESTORE_STATE, () => state.restore(event.data))
		.is(EventBus.Type.DIR_CHANGE, () => {
			if (event.data.currentDir) state.currentDir = event.data.currentDir;
			state.files = event.data.files;
		})

		.is(EventBus.Type.PLAY, () => {
			state.isPlaying = true;
		})
		.is(EventBus.Type.PAUSE, () => {
			state.isPlaying = false;
		})
		// .is(EventBus.Type.PLAY_TRACK, () => {
		// 	state.currentTrack = event.data.path;
		// 	state.isPlaying = true;
		// })
});
