import { create } from 'zustand'

export const useRepoStore = create((set) => ({
  currentRepoId: null,
  repoData: null,
  repositories: [],

  setCurrentRepoId: (id) => set({ currentRepoId: id }),
  setRepoData: (data) => set({ repoData: data }),
  setRepositories: (repos) => set({ repositories: repos }),

  clearCurrentRepo: () => set({
    currentRepoId: null,
    repoData: null
  }),

  addRepository: (repo) => set((state) => ({
    repositories: [...state.repositories, repo]
  })),

  updateRepository: (id, updates) => set((state) => ({
    repositories: state.repositories.map((repo) =>
      repo.repoId === id ? { ...repo, ...updates } : repo
    )
  }))
}))
