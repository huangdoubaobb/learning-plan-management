import { ref } from 'vue'

export const useMultiImageUpload = ({ api, message, uploadPath, fieldName = 'files' }) => {
  const imageInput = ref(null)
  const imageFiles = ref([])
  const imagePreviews = ref([])

  const clearImagePreviews = () => {
    imagePreviews.value.forEach((item) => URL.revokeObjectURL(item.url))
    imagePreviews.value = []
  }

  const resetImageState = () => {
    clearImagePreviews()
    imageFiles.value = []
    if (imageInput.value) {
      imageInput.value.value = ''
    }
  }

  const triggerImagePick = () => {
    if (imageInput.value) {
      imageInput.value.value = ''
      imageInput.value.click()
    }
  }

  const handleImageChange = (event) => {
    const files = Array.from(event?.target?.files || [])
    if (!files.length) return

    const nextFiles = []
    const nextPreviews = []

    for (const file of files) {
      if (!file.type || !file.type.startsWith('image/')) {
        message.warning('仅支持图片文件')
        continue
      }
      if (file.size > 10 * 1024 * 1024) {
        message.warning('图片大小不能超过 10MB')
        continue
      }
      nextFiles.push(file)
      nextPreviews.push({ url: URL.createObjectURL(file), name: file.name })
    }

    if (!nextFiles.length) return

    clearImagePreviews()
    imageFiles.value = nextFiles
    imagePreviews.value = nextPreviews
  }

  const removeImage = (index) => {
    const removed = imagePreviews.value.splice(index, 1)
    if (removed.length) {
      URL.revokeObjectURL(removed[0].url)
    }
    imageFiles.value.splice(index, 1)
  }

  const uploadImages = async (id) => {
    if (!id || !imageFiles.value.length) return

    const formData = new FormData()
    imageFiles.value.forEach((file) => formData.append(fieldName, file))

    await api.post(uploadPath(id), formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }

  return {
    imageInput,
    imageFiles,
    imagePreviews,
    resetImageState,
    triggerImagePick,
    handleImageChange,
    removeImage,
    uploadImages
  }
}
