package pl.soulsnaps.domain.interactor

import pl.soulsnaps.domain.MemoryRepository

class DeleteMemoryUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memoryId: Int) {
        println("DEBUG: DeleteMemoryUseCase.invoke() - deleting memory with ID: $memoryId")
        memoryRepository.deleteMemory(memoryId)
        println("DEBUG: DeleteMemoryUseCase.invoke() - memory deleted successfully")
    }
}
