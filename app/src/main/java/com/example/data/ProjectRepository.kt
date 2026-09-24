package com.example.data

import com.example.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<Project>> = dao.getAllProjects().map { entities ->
        entities.map { ProjectConverter.fromEntity(it) }
    }

    suspend fun getProjectById(id: Long): Project? {
        val entity = dao.getProjectById(id) ?: return null
        return ProjectConverter.fromEntity(entity)
    }

    suspend fun saveProject(project: Project): Long {
        val entity = ProjectConverter.toEntity(project.copy(updatedAt = System.currentTimeMillis()))
        return if (project.id == 0L) {
            dao.insertProject(entity)
        } else {
            dao.updateProject(entity)
            project.id
        }
    }

    suspend fun duplicateProject(project: Project): Long {
        val duplicated = project.copy(
            id = 0L,
            title = "${project.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return dao.insertProject(ProjectConverter.toEntity(duplicated))
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }
}
