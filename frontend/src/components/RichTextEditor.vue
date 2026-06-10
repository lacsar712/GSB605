<template>
  <div class="rich-text-editor">
    <div class="toolbar">
      <el-button-group>
        <el-button size="small" @click="runCommand('bold')">加粗</el-button>
        <el-button size="small" @click="runCommand('italic')">斜体</el-button>
        <el-button size="small" @click="runCommand('underline')">下划线</el-button>
      </el-button-group>
      <el-button-group>
        <el-button size="small" @click="runCommand('insertUnorderedList')">无序列表</el-button>
        <el-button size="small" @click="runCommand('insertOrderedList')">有序列表</el-button>
        <el-button size="small" @click="insertLink">链接</el-button>
      </el-button-group>
      <el-button size="small" @click="clearFormatting">清除格式</el-button>
    </div>

    <div
      ref="editorRef"
      class="editor"
      contenteditable="true"
      :data-placeholder="placeholder"
      @input="emitContent"
      @blur="emitContent"
    />
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { sanitizeRichText } from '@/utils/richText'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容'
  }
})

const emit = defineEmits(['update:modelValue'])
const editorRef = ref(null)

const syncEditor = (html) => {
  const sanitized = sanitizeRichText(html)
  if (editorRef.value && editorRef.value.innerHTML !== sanitized) {
    editorRef.value.innerHTML = sanitized
  }
}

const emitContent = () => {
  const html = sanitizeRichText(editorRef.value?.innerHTML || '')
  if (editorRef.value && editorRef.value.innerHTML !== html) {
    editorRef.value.innerHTML = html
  }
  emit('update:modelValue', html)
}

const runCommand = async (command) => {
  editorRef.value?.focus()
  document.execCommand(command)
  await nextTick()
  emitContent()
}

const clearFormatting = async () => {
  editorRef.value?.focus()
  document.execCommand('removeFormat')
  document.execCommand('unlink')
  await nextTick()
  emitContent()
}

const insertLink = async () => {
  const { value } = await ElMessageBox.prompt('请输入链接地址', '插入链接', {
    inputPlaceholder: 'https://example.com'
  }).catch(() => ({ value: '' }))

  if (!value) return

  editorRef.value?.focus()
  document.execCommand('createLink', false, value)
  await nextTick()
  emitContent()
}

watch(() => props.modelValue, syncEditor)

onMounted(() => {
  syncEditor(props.modelValue)
})
</script>

<style lang="scss" scoped>
.rich-text-editor {
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-light);
}

.editor {
  min-height: 220px;
  padding: 16px;
  line-height: 1.7;
  outline: none;
  white-space: normal;
  word-break: break-word;

  &:empty::before {
    content: attr(data-placeholder);
    color: var(--el-text-color-placeholder);
  }
}
</style>
