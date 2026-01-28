<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { type Model, ModelService } from '../services/ModelService'

const models = ref<Model[]>([])
const newModel = ref<Model>({
    name: '',
    provider: 'openai',
    modelType: 'chat',
    tags: []
})

async function loadModels() {
    models.value = await ModelService.list()
}

async function addModel() {
    await ModelService.create(newModel.value)
    newModel.value = { name: '', provider: 'openai', modelType: 'chat', tags: [] }
    await loadModels()
}

onMounted(() => {
    loadModels()
})
</script>

<template>
  <div class="models-page">
    <h1>Model Registry</h1>
    
    <div class="add-model">
        <h3>Add Model</h3>
        <input v-model="newModel.name" placeholder="Name (e.g. gpt-4)" />
        <select v-model="newModel.provider">
            <option value="openai">OpenAI</option>
            <option value="ollama">Ollama</option>
        </select>
        <button @click="addModel">Add</button>
    </div>

    <ul>
        <li v-for="m in models" :key="m.id">
            <strong>{{ m.name }}</strong> ({{ m.provider }}) - Tags: {{ m.tags }}
        </li>
    </ul>
  </div>
</template>

<style scoped>
.models-page { padding: 20px; }
.add-model { border: 1px solid #ccc; padding: 10px; margin-bottom: 20px; }
input, select, button { margin-right: 10px; padding: 5px; }
</style>
