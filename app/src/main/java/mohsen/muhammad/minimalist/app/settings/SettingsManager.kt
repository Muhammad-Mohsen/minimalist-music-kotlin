package mohsen.muhammad.minimalist.app.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.app.player.repeatAnimations
import mohsen.muhammad.minimalist.app.player.repeatIcons
import mohsen.muhammad.minimalist.app.player.shuffleIcons
import mohsen.muhammad.minimalist.core.Moirai
import mohsen.muhammad.minimalist.core.OnSeekBarChangeListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.core.ext.animateLayoutMargins
import mohsen.muhammad.minimalist.core.ext.context
import mohsen.muhammad.minimalist.core.ext.fadeIn
import mohsen.muhammad.minimalist.core.ext.fadeOut
import mohsen.muhammad.minimalist.core.ext.resources
import mohsen.muhammad.minimalist.core.ext.setImageDrawable
import mohsen.muhammad.minimalist.core.ext.setStroke
import mohsen.muhammad.minimalist.core.ext.slideY
import mohsen.muhammad.minimalist.data.Const
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.FabMenu
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding
import kotlin.math.roundToInt


/**
 * Created by muhammad.mohsen on 11/16/2023.
 * manages the settings bottom sheet show/hide, and click handlers
 */
class SettingsManager(mainBinding: MainFragmentBinding) : EventBus.Subscriber {

	private val binding = mainBinding.layoutSettings
	private val controls = mainBinding.layoutControls2

	@SuppressLint("ClickableViewAccessibility")
	fun initialize() {

		// event bus subscription
		EventBus.subscribe(this)

		// show settings sheet
		controls.buttonSettings.setOnClickListener {
			binding.settingsSheet.animateLayoutMargins(R.dimen.spacingZero, ANIM_DURATION, Const.exponentialInterpolator)
			binding.viewScrim.fadeIn(ANIM_DURATION)
		}

		// gesture recognizer
		var intermediateY = 0F
		binding.viewScrim.setOnTouchListener { _, event ->
			return@setOnTouchListener when (event.action) {
				MotionEvent.ACTION_DOWN -> {
					intermediateY = event.rawY
					true
				}
				MotionEvent.ACTION_MOVE -> {
					val delta = intermediateY - event.rawY // calculate the delta
					intermediateY = event.rawY // and update the y

					binding.settingsSheet.slideY(delta.toInt())

					true
				}
				MotionEvent.ACTION_UP -> {
					if ((binding.settingsSheet.layoutParams as FrameLayout.LayoutParams).bottomMargin < FLICK_THRESHOLD) {
						binding.settingsSheet.animateLayoutMargins(R.dimen.spacingZero, R.dimen.spacingZero, R.dimen.spacingZero, R.dimen.settingsHiddenMargin, ANIM_DURATION, Const.exponentialInterpolator)
						binding.viewScrim.fadeOut(ANIM_DURATION)

					} else binding.settingsSheet.animateLayoutMargins(R.dimen.spacingZero, ANIM_DURATION, Const.exponentialInterpolator)

					true
				}
				else -> false
			}
		}

		binding.viewScrim.setOnClickListener {
			binding.settingsSheet.animateLayoutMargins(R.dimen.spacingZero, R.dimen.spacingZero, R.dimen.spacingZero, R.dimen.settingsHiddenMargin, ANIM_DURATION, Const.exponentialInterpolator)
			binding.viewScrim.fadeOut(ANIM_DURATION)
		}

		binding.buttonThemeSystem.setOnClickListener {
			selectThemeButton(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
			setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
		}
		binding.buttonThemeLight.setOnClickListener {
			selectThemeButton(AppCompatDelegate.MODE_NIGHT_NO)
			setNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		}
		binding.buttonThemeDark.setOnClickListener {
			selectThemeButton(AppCompatDelegate.MODE_NIGHT_YES)
			setNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		}

		// seek jump
		binding.seekJump.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				if (!p2) return // if not initiated by a user

				State.seekJump = ((p1.toDouble() / SEEK_JUMP_STEP).roundToInt() * SEEK_JUMP_STEP)
				p0?.progress = State.seekJump
				binding.seekJumpText.text = binding.resources.getString(R.string.seekJumpValue, State.seekJump)
			}
		})

		// sleep timer
		binding.sleepTimerDuration.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				if (!p2) return // not initiated by user

				State.sleepTimer = ((p1.toDouble() / SEEK_JUMP_STEP).roundToInt() * SEEK_JUMP_STEP)
				p0?.progress = State.sleepTimer
				binding.sleepTimerTextDuration.text = formatTime(State.sleepTimer * 60)
			}
		})
		binding.sleepTimerToggle.setOnClickListener {
			val isActive = binding.sleepTimerToggle.tag as? Boolean ?: false
			setSleepTimerUi(isActive)
		}

		// repeat
		binding.buttonRepeat.setOnClickListener {
			binding.iconRepeat.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_REPEAT))
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.CYCLE_REPEAT))
		}
		// shuffle
		binding.buttonShuffle.setOnClickListener {
			binding.iconShuffle.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_SHUFFLE))
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.CYCLE_SHUFFLE))
		}

		binding.buttonPrivacyPolicy.setOnClickListener {
			val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Const.PRIVACY_POLICY_URL))
			startActivity(binding.context, browserIntent, null)
		}
	}

	private fun updateMetadata() {
		selectThemeButton(State.nightMode)
		setNightMode(State.nightMode)

		binding.seekJump.progress = State.seekJump
		binding.seekJumpText.text = binding.resources.getString(R.string.seekJumpValue, State.seekJump)
		binding.sleepTimerDuration.progress = State.sleepTimer
		binding.sleepTimerTextDuration.text = formatTime(State.sleepTimer * 60)
		binding.iconRepeat.setImageDrawable(repeatIcons[State.playlist.repeat])
		binding.iconShuffle.setImageDrawable(if (State.playlist.shuffle) shuffleIcons[1] else shuffleIcons[0])
	}

	private fun getButtonAnimationByIndex(buttonIndex: Int): Int {
		return when (buttonIndex) {
			FabMenu.BUTTON_NEXT -> R.drawable.anim_next
			FabMenu.BUTTON_REPEAT -> {
				repeatAnimations[(State.playlist.repeat + 1) % repeatAnimations.size]
			}
			FabMenu.BUTTON_SHUFFLE -> {
				if (State.playlist.shuffle) R.drawable.anim_shuffle_inactive
				else R.drawable.anim_shuffle_active
			}
			else -> R.drawable.anim_next // FabMenu.BUTTON_PREV
		}
	}

	private fun selectThemeButton(mode: Int) {
		val elev = binding.context.resources.getDimension(R.dimen.elevationMedium)
		binding.buttonThemeSystem.elevation = if (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) elev else 0F
		binding.buttonThemeLight.elevation = if (mode == AppCompatDelegate.MODE_NIGHT_NO) elev else 0F
		binding.buttonThemeDark.elevation = if (mode == AppCompatDelegate.MODE_NIGHT_YES) elev else 0F

		binding.buttonThemeSystem.setStroke(if (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) 1 else 0, R.color.subSettingsBorder)
		binding.buttonThemeLight.setStroke(if (mode == AppCompatDelegate.MODE_NIGHT_NO) 1 else 0, R.color.subSettingsBorder)
		binding.buttonThemeDark.setStroke(if (mode == AppCompatDelegate.MODE_NIGHT_YES) 1 else 0, R.color.subSettingsBorder)
	}
	private fun setNightMode(mode: Int) {
		State.nightMode = mode
		AppCompatDelegate.setDefaultNightMode(mode)
	}

	private fun setSleepTimerUi(isActive: Boolean) {
		val animId = if (!isActive) R.drawable.anim_stopwatch else R.drawable.anim_stopwatch_reverse
		binding.sleepTimerToggle.animateDrawable(animId)

		binding.sleepTimerProgress.progress = if (isActive) 0 else State.sleepTimer
		binding.sleepTimerTextProgress.text = formatTime(State.sleepTimer * 60, true)
		binding.sleepTimerTextProgress.visibility = if (isActive) View.GONE else View.VISIBLE

		if (isActive) SleepTimer.cancel()
		else SleepTimer.start(State.sleepTimer)

		binding.sleepTimerToggle.tag = !isActive // update the tag
	}
	private fun formatTime(seconds: Int, withSeconds: Boolean = false): String {
		fun pad(num: Int) = num.toString().padStart(2, '0')

		return if (withSeconds) "${pad(seconds / 60 / 60)}:${pad(seconds / 60 % 60)}:${pad(seconds % 60)}"
		else "${pad(seconds / 60 / 60)}:${pad((seconds / 60 % 60))}"
	}

	override fun receive(data: EventBus.EventData) {
		Moirai.MAIN.post { // make sure we're running on main

			if (data !is SystemEvent) return@post // not interested in event types other then SystemEvent
			if (data.source == EventSource.CONTROLS) return@post // not interested in events that were sent from here

			when (data.type) {
				EventType.METADATA_UPDATE -> updateMetadata()
				EventType.SLEEP_TIMER_TICK -> {
					// ignore the tick if settings sheet is hidden
					if ((binding.settingsSheet.layoutParams as FrameLayout.LayoutParams).bottomMargin != 0) return@post

					binding.sleepTimerProgress.progress = data.extras.toInt() / 60 // convert to minutes
					binding.sleepTimerTextProgress.text = formatTime(data.extras.toInt(), true)
				}
				EventType.SLEEP_TIMER_FINISH -> {
					setSleepTimerUi(true)
				}
			}
		}
	}

	companion object {
		const val ANIM_DURATION = 400L
		const val FLICK_THRESHOLD = -200 // the settings sheet will hide if its bottom margin goes past this value
		const val SEEK_JUMP_STEP = 5

	}
}
