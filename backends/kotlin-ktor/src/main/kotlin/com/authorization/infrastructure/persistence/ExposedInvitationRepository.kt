package com.authorization.infrastructure.persistence

import com.authorization.config.Config
import com.authorization.domain.invitation.Repository
import com.authorization.domain.invitation.Vo
import org.jetbrains.exposed.sql.Database

class ExposedInvitationRepository(private val db: Database, private val cfg: Config) : Repository {
    override suspend fun getCurrent(): Vo? = TODO()
    override suspend fun issue(): Vo = TODO()
    override suspend fun findByToken(token: String): Vo? = TODO()
}
