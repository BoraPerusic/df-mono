import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ModelsView from '../ModelsView.vue'
import { ModelService } from '@/services/ModelService'

// Mock ModelService with named export
vi.mock('@/services/ModelService', () => ({
    ModelService: {
        list: vi.fn(),
        create: vi.fn()
    }
}))

describe('ModelsView', () => {
    it('renders correctly', () => {
        (ModelService.list as any).mockResolvedValue([])
        const wrapper = mount(ModelsView)
        expect(wrapper.text()).toContain('Model Registry')
    })

    it('loads models on mount', async () => {
        (ModelService.list as any).mockResolvedValue([
            { id: 1, name: 'Test Model', provider: 'openai', modelType: 'chat', tags: [] }
        ])
        const wrapper = mount(ModelsView)

        // Wait for promises
        await new Promise(resolve => setTimeout(resolve, 0))
        await wrapper.vm.$nextTick()

        expect(wrapper.text()).toContain('Test Model')
        expect(wrapper.text()).toContain('openai')
    })
})
