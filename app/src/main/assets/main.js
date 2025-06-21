addEventListener("DOMContentLoaded", () => {
	if (location.href.includes('mode=permission')) document.body.setAttribute('mode', 'permission'); // permission layout
	// else EventBus.dispatch({ type: EventBus.Type.RESTORE_STATE, target: EventBus.Target.MAIN }); // get everything ready // not sure if needed

	setTimeout(() => document.body.classList.add('ready')); // reveal animation
});

const state = new MockState();

EventBus.subscribe(event => {
	when(event.type)
		.is(EventBus.Type.INSETS, () => {
			document.body.style = `--inset-top: ${event.data.top / devicePixelRatio}px; --inset-bottom: ${event.data.bottom / devicePixelRatio}px;`;
		});
});
