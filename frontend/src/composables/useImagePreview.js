import { ref } from 'vue'

export const useImagePreview = (resolveUrl = (value) => value) => {
  const showImagePreview = ref(false)
  const previewImages = ref([])
  const previewIndex = ref(0)

  const openImagePreview = (images, index = 0) => {
    const list = Array.isArray(images) ? images.map(resolveUrl).filter(Boolean) : []
    previewImages.value = list
    previewIndex.value = Math.max(0, Math.min(index, Math.max(list.length - 1, 0)))
    showImagePreview.value = true
  }

  const closeImagePreview = () => {
    showImagePreview.value = false
    previewImages.value = []
    previewIndex.value = 0
  }

  const prevImage = () => {
    if (previewIndex.value > 0) {
      previewIndex.value -= 1
    }
  }

  const nextImage = () => {
    if (previewIndex.value < previewImages.value.length - 1) {
      previewIndex.value += 1
    }
  }

  return {
    showImagePreview,
    previewImages,
    previewIndex,
    openImagePreview,
    closeImagePreview,
    prevImage,
    nextImage
  }
}
