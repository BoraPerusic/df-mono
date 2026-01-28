import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardView from '../DashboardView.vue'

describe('DashboardView', () => {
    it('renders properly', async () => {
        global.fetch = vi.fn().mockResolvedValue({
            json: () => Promise.resolve([])
        })

        const wrapper = mount(DashboardView)
        await wrapper.vm.$nextTick()

        expect(wrapper.text()).toContain('Dashboard')
        expect(wrapper.text()).toContain('Observability Dashboard')
    })
})
