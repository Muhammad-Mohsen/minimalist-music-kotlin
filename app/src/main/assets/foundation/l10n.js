const L10N = (() => {

	const elements = document.querySelectorAll('[aria-label], [l10n]');

});

const translations = {
	'play': {
		'en': 'play',
		'es': 'play',
		'ru': 'play'
	},
	'pause': {

	}
}

/* EN
	<string name="appName">Minimalist</string>
	<string name="minimalistMusicPlayer">Minimalist Music Player</string>
	<string name="welcomeMessage">Welcome!</string>

	<string name="descriptionRecyclerItemTypeTrack">track</string>

	<string name="blankTimestamp" translatable="false">--:--</string>

	<string name="breadcrumbSeparator" translatable="false">/</string> <!-- other symbols to try out: ›> | -->

	<string name="permissionMessage">Storage Permission is required</string>
	<string name="permissionMessageSubtitle">Tap anywhere to grant the permission</string>

	<string name="notificationChannelName">Playback Notification</string>
	<string name="notificationChannelDescription">Displays the app\'s playback notification</string>
	<string name="album_art">Album Art</string>
	<string name="system">System</string>
	<string name="dark">Dark</string>
	<string name="light">Light</string>
	<string name="seekJump">Seek Jump</string>
	<string name="shuffle">Shuffle</string>
	<string name="repeat">Repeat</string>
	<string name="privacyPolicy">Privacy Policy</string>
	<string name="settingsButtonContentDescription">Settings</string>
	<string name="albumArtButtonContentDescription">Expand Collapse Album Art</string>
	<string name="previousButtonContentDescription">Previous</string>
	<string name="playPauseButtonContentDescription">Play/Pause</string>
	<string name="nextButtonContentDescription">Next</string>
	<string name="selectedTracks">Play Selected Tracks</string>
	<string name="backButton">Back</string>
	<string name="addToSelected">Add To Selected</string>
	<string name="cancelButton">Cancel</string>
	<string name="search">Search</string>

	<plurals name="selectedCount">
		<item quantity="one">%1$d Track Selected</item>
		<item quantity="other">%1$d Tracks Selected</item>
	</plurals>
	<string name="selectedCount" translatable="false">(%1$d)</string>

	<string name="sleepTimer">Sleep Timer</string>
	<string name="sleepTimerToggle">Toggle Sleep Timer</string>

	<string name="seekJumpValue">%1$d Seconds</string>
	<string name="equalizer">Equalizer</string>
	<string name="noEqualizer">No equalizer found</string>
	<string name="playbackSpeed">Playback Speed</string>
	<string name="playbackSpeedValue" translatable="false">%1$.2fx</string>
*/

/* pt-BR
	<string name="appName">Minimalista</string>
	<string name="minimalistMusicPlayer">Player de Música Minimalista</string>
	<string name="welcomeMessage">Bem-vindo!</string>
	<string name="descriptionRecyclerItemTypeTrack">faixa de áudio</string>
	<string name="permissionMessage">É necessária permissão de armazenamento</string>
	<string name="permissionMessageSubtitle">Toque em qualquer lugar para conceder a permissão</string>
	<string name="notificationChannelName">Notificação de reprodução</string>
	<string name="notificationChannelDescription">Exibe a notificação de reprodução do aplicativo</string>
	<string name="album_art">Arte do álbum</string>
	<string name="system">Sistema</string>
	<string name="dark">Escuro</string>
	<string name="light">Claro</string>
	<string name="seekJump">Período de Salto de Busca</string>
	<string name="shuffle">Misturar</string>
	<string name="repeat">Repetir</string>
	<string name="privacyPolicy">Política de Privacidade</string>
	<string name="settingsButtonContentDescription">Configurações</string>
	<string name="albumArtButtonContentDescription">Expandir Recolher arte do álbum</string>
	<string name="previousButtonContentDescription">Anterior</string>
	<string name="playPauseButtonContentDescription">Reproduzir/Pausar</string>
	<string name="nextButtonContentDescription">Próxima</string>
	<string name="selectedTracks">Reproduzir faixas selecionadas</string>
	<string name="backButton">Voltar</string>
	<string name="addToSelected">Adicionar aos selecionados</string>
	<string name="cancelButton">Cancelar</string>
	<string name="search">Pesquisar</string>

	<plurals name="selectedCount" tools:ignore="MissingQuantity">
		<item quantity="one">%1$d Faixas Selecionadas</item>
		<item quantity="other">%1$d Faixa Selecionada</item>
	</plurals>

	<string name="sleepTimer">Temporizador</string>
	<string name="sleepTimerToggle">Alternar Temporizador</string>

	<string name="seekJumpValue">%1$d Segundas</string>
	<string name="equalizer">Equalizador</string>
	<string name="noEqualizer">Nenhum equalizador encontrado</string>
	<string name="playbackSpeed">Velocidade de reprodução</string>
*/