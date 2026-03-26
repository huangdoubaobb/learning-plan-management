<template>
  <div class="characters-container">
    <div ref="purpleRef" class="char purple" :style="purpleStyle">
      <div class="eyes" :style="purpleEyesStyle">
        <EyeBall
          v-for="index in 2"
          :key="`purple-${index}`"
          :size="18"
          :pupil-size="7"
          :max-distance="5"
          eye-color="white"
          pupil-color="#2d2d2d"
          :is-blinking="isPurpleBlinking"
          :force-look-x="purpleLookX"
          :force-look-y="purpleLookY"
        />
      </div>
    </div>

    <div ref="blackRef" class="char black" :style="blackStyle">
      <div class="eyes" :style="blackEyesStyle">
        <EyeBall
          v-for="index in 2"
          :key="`black-${index}`"
          :size="16"
          :pupil-size="6"
          :max-distance="4"
          eye-color="white"
          pupil-color="#2d2d2d"
          :is-blinking="isBlackBlinking"
          :force-look-x="blackLookX"
          :force-look-y="blackLookY"
        />
      </div>
    </div>

    <div ref="orangeRef" class="char orange" :style="orangeStyle">
      <div class="eyes" :style="orangeEyesStyle">
        <Pupil
          v-for="index in 2"
          :key="`orange-${index}`"
          :size="12"
          :max-distance="5"
          pupil-color="#2d2d2d"
          :force-look-x="hiding ? -5 : undefined"
          :force-look-y="hiding ? -4 : undefined"
        />
      </div>
    </div>

    <div ref="yellowRef" class="char yellow" :style="yellowStyle">
      <div class="eyes" :style="yellowEyesStyle">
        <Pupil
          v-for="index in 2"
          :key="`yellow-${index}`"
          :size="12"
          :max-distance="5"
          pupil-color="#2d2d2d"
          :force-look-x="hiding ? -5 : undefined"
          :force-look-y="hiding ? -4 : undefined"
        />
      </div>
      <div class="mouth" :style="yellowMouthStyle" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import EyeBall from './EyeBall.vue'
import Pupil from './Pupil.vue'

const props = defineProps({
  isTyping: { type: Boolean, default: false },
  hasSecret: { type: Boolean, default: false },
  secretVisible: { type: Boolean, default: false }
})

const mouseX = ref(0)
const mouseY = ref(0)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

const purpleRef = ref(null)
const blackRef = ref(null)
const yellowRef = ref(null)
const orangeRef = ref(null)

const purplePos = reactive({ faceX: 0, faceY: 0, bodySkew: 0 })
const blackPos = reactive({ faceX: 0, faceY: 0, bodySkew: 0 })
const yellowPos = reactive({ faceX: 0, faceY: 0, bodySkew: 0 })
const orangePos = reactive({ faceX: 0, faceY: 0, bodySkew: 0 })

const hiding = computed(() => props.hasSecret && props.secretVisible)
const leaning = computed(() => props.isTyping || (props.hasSecret && !props.secretVisible))

const calcPos = (element, target) => {
  if (!element) return
  const rect = element.getBoundingClientRect()
  const dx = mouseX.value - (rect.left + rect.width / 2)
  const dy = mouseY.value - (rect.top + rect.height / 3)
  target.faceX = Math.max(-15, Math.min(15, dx / 20))
  target.faceY = Math.max(-10, Math.min(10, dy / 30))
  target.bodySkew = Math.max(-6, Math.min(6, -dx / 120))
}

let rafId = 0
const tick = () => {
  calcPos(purpleRef.value, purplePos)
  calcPos(blackRef.value, blackPos)
  calcPos(yellowRef.value, yellowPos)
  calcPos(orangeRef.value, orangePos)
  rafId = requestAnimationFrame(tick)
}

const onMouseMove = (event) => {
  mouseX.value = event.clientX
  mouseY.value = event.clientY
}

const setupBlink = (target) => {
  let timerId
  const go = () => {
    timerId = window.setTimeout(() => {
      target.value = true
      window.setTimeout(() => {
        target.value = false
        go()
      }, 150)
    }, Math.random() * 4000 + 3000)
  }
  go()
  return () => clearTimeout(timerId)
}

let stopPurpleBlink
let stopBlackBlink

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
  stopPurpleBlink = setupBlink(isPurpleBlinking)
  stopBlackBlink = setupBlink(isBlackBlinking)
  rafId = requestAnimationFrame(tick)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  cancelAnimationFrame(rafId)
  stopPurpleBlink?.()
  stopBlackBlink?.()
})

watch(() => props.isTyping, (value) => {
  if (value) {
    isLookingAtEachOther.value = true
    window.setTimeout(() => {
      isLookingAtEachOther.value = false
    }, 800)
  } else {
    isLookingAtEachOther.value = false
  }
})

let peekTimer
watch([() => props.hasSecret, () => props.secretVisible], () => {
  clearTimeout(peekTimer)
  if (props.hasSecret && props.secretVisible) {
    peekTimer = window.setTimeout(() => {
      isPurplePeeking.value = true
      window.setTimeout(() => {
        isPurplePeeking.value = false
      }, 800)
    }, Math.random() * 3000 + 2000)
  } else {
    isPurplePeeking.value = false
  }
})

const purpleStyle = computed(() => ({
  height: leaning.value ? '440px' : '400px',
  transform: hiding.value
    ? 'skewX(0deg)'
    : leaning.value
      ? `skewX(${purplePos.bodySkew - 12}deg) translateX(40px)`
      : `skewX(${purplePos.bodySkew}deg)`
}))

const purpleEyesStyle = computed(() => ({
  left: hiding.value ? '20px' : isLookingAtEachOther.value ? '55px' : `${45 + purplePos.faceX}px`,
  top: hiding.value ? '35px' : isLookingAtEachOther.value ? '65px' : `${40 + purplePos.faceY}px`,
  gap: '32px'
}))

const purpleLookX = computed(() => hiding.value ? (isPurplePeeking.value ? 4 : -4) : isLookingAtEachOther.value ? 3 : undefined)
const purpleLookY = computed(() => hiding.value ? (isPurplePeeking.value ? 5 : -4) : isLookingAtEachOther.value ? 4 : undefined)

const blackStyle = computed(() => ({
  transform: hiding.value
    ? 'skewX(0deg)'
    : isLookingAtEachOther.value
      ? `skewX(${blackPos.bodySkew * 1.5 + 10}deg) translateX(20px)`
      : leaning.value
        ? `skewX(${blackPos.bodySkew * 1.5}deg)`
        : `skewX(${blackPos.bodySkew}deg)`
}))

const blackEyesStyle = computed(() => ({
  left: hiding.value ? '10px' : isLookingAtEachOther.value ? '32px' : `${26 + blackPos.faceX}px`,
  top: hiding.value ? '28px' : isLookingAtEachOther.value ? '12px' : `${32 + blackPos.faceY}px`,
  gap: '24px'
}))

const blackLookX = computed(() => hiding.value ? -4 : isLookingAtEachOther.value ? 0 : undefined)
const blackLookY = computed(() => hiding.value ? -4 : isLookingAtEachOther.value ? -4 : undefined)

const orangeStyle = computed(() => ({
  transform: hiding.value ? 'skewX(0deg)' : `skewX(${orangePos.bodySkew}deg)`
}))

const orangeEyesStyle = computed(() => ({
  left: hiding.value ? '50px' : `${82 + orangePos.faceX}px`,
  top: hiding.value ? '85px' : `${90 + orangePos.faceY}px`,
  gap: '32px'
}))

const yellowStyle = computed(() => ({
  transform: hiding.value ? 'skewX(0deg)' : `skewX(${yellowPos.bodySkew}deg)`
}))

const yellowEyesStyle = computed(() => ({
  left: hiding.value ? '20px' : `${52 + yellowPos.faceX}px`,
  top: hiding.value ? '35px' : `${40 + yellowPos.faceY}px`,
  gap: '24px'
}))

const yellowMouthStyle = computed(() => ({
  left: hiding.value ? '10px' : `${40 + yellowPos.faceX}px`,
  top: hiding.value ? '88px' : `${88 + yellowPos.faceY}px`
}))
</script>

<style scoped>
.characters-container {
  position: relative;
  width: min(540px, 100%);
  height: 400px;
}

.char {
  position: absolute;
  bottom: 0;
  transition: all 0.7s ease-in-out;
  transform-origin: bottom center;
}

.purple {
  left: 70px;
  width: 180px;
  background: linear-gradient(180deg, #f43f5e 0%, #fb7185 48%, #fda4af 100%);
  border-radius: 10px 10px 0 0;
  z-index: 1;
  box-shadow: 0 18px 28px rgba(244, 63, 94, 0.24);
}

.black {
  left: 240px;
  width: 120px;
  height: 310px;
  background: #2d2d2d;
  border-radius: 8px 8px 0 0;
  z-index: 2;
}

.orange {
  left: 0;
  width: 240px;
  height: 200px;
  background: #ff9b6b;
  border-radius: 120px 120px 0 0;
  z-index: 3;
}

.yellow {
  left: 310px;
  width: 140px;
  height: 230px;
  background: #e8d754;
  border-radius: 70px 70px 0 0;
  z-index: 4;
}

.eyes {
  position: absolute;
  display: flex;
  transition: all 0.7s ease-in-out;
}

.mouth {
  position: absolute;
  width: 80px;
  height: 4px;
  background: #2d2d2d;
  border-radius: 4px;
  transition: all 0.2s ease-out;
}

@media (max-width: 1200px) {
  .characters-container {
    transform: scale(0.92);
    transform-origin: center bottom;
  }
}
</style>
