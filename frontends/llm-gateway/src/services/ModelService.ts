export interface Model {
    id?: number
    name: string
    description?: string
    provider: string
    modelType: string
    tags?: string[]
}

export const ModelService = {
    async list(): Promise<Model[]> {
        const res = await fetch('/api/v1/models')
        return res.json()
    },
    async create(model: Model): Promise<Model> {
        const res = await fetch('/api/v1/models', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(model)
        })
        return res.json()
    }
}
