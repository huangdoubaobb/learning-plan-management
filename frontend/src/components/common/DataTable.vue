<template>
  <div class="table-wrap">
    <table class="table">
      <thead>
        <tr>
          <th v-for="column in columns" :key="column.key">{{ column.label }}</th>
          <th v-if="actions">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, index) in data" :key="row.id || index">
          <td v-for="column in columns" :key="column.key">
            <slot :name="column.key" :row="row">
              {{ row[column.key] }}
            </slot>
          </td>
          <td v-if="actions">
            <div class="action-buttons">
              <slot name="actions" :row="row"></slot>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
defineProps({
  data: {
    type: Array,
    required: true
  },
  columns: {
    type: Array,
    required: true
  },
  actions: {
    type: Boolean,
    default: false
  }
})
</script>
