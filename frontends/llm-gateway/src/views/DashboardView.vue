<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface LogEntry {
  id: number
  modelName: string
  promptText: string
  durationMs: number
  status: string
  createdAt: string
}

const logs = ref<LogEntry[]>([])
const searchQuery = ref('')

async function fetchLogs() {
  const url = searchQuery.value 
      ? `/api/v1/observability/logs?query=${encodeURIComponent(searchQuery.value)}`
      : '/api/v1/observability/logs'
      
  const res = await fetch(url)
  logs.value = await res.json()
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="dashboard">
    <h1>Observability Dashboard</h1>
    
    <div class="search-bar">
        <input v-model="searchQuery" placeholder="Search logs (full text)..." @keyup.enter="fetchLogs" />
        <button @click="fetchLogs">Search</button>
    </div>

    <table class="logs-table">
        <thead>
            <tr>
                <th>Time</th>
                <th>Model</th>
                <th>Prompt</th>
                <th>Duration (ms)</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="log in logs" :key="log.id">
                <td>{{ new Date(log.createdAt).toLocaleString() }}</td>
                <td>{{ log.modelName }}</td>
                <td class="prompt-col">{{ log.promptText.substring(0, 50) }}...</td>
                <td>{{ log.durationMs }}</td>
                <td>{{ log.status }}</td>
            </tr>
        </tbody>
    </table>
  </div>
</template>

<style scoped>
.dashboard { padding: 20px; }
.search-bar { margin-bottom: 20px; }
.logs-table { width: 100%; border-collapse: collapse; }
.logs-table th, .logs-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
.prompt-col { font-family: monospace; color: #555; }
</style>
