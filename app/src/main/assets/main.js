function main() {

}
// another option is to just send the data automatically onload from native

addEventListener("DOMContentLoaded", (event) => {
	if (location.href.includes('mode=permission')) document.body.setAttribute('mode', 'permission'); // permission layout
	else EventBus.dispatch({ type: EventBus.Type.RESTORE_STATE, target: EventBus.Target.MAIN }); // get everything ready

	setTimeout(() => document.body.classList.add('ready'));
});
