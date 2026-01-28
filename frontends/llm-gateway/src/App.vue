```html
<script setup lang="ts">
import { ref } from 'vue'
import ModelsView from './views/ModelsView.vue'
import DashboardView from './views/DashboardView.vue'

const currentView = ref('models')
</script>

<template>
  <div class="app-container">
      <nav>
          <button @click="currentView = 'models'">Models</button>
          <button @click="currentView = 'dashboard'">Dashboard</button>
      </nav>
      
      <main>
          <ModelsView v-if="currentView === 'models'" />
          <DashboardView v-if="currentView === 'dashboard'" />
      </main>
  </div>
</template>


<style>
nav { padding: 10px; background: #f0f0f0; margin-bottom: 20px;}
nav button { margin-right: 10px; padding: 5px 10px; cursor: pointer; }

header {
  line-height: 1.5;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
