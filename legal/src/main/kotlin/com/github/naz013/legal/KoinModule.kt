package com.github.naz013.legal

import com.github.naz013.legal.impl.FirebaseLegalDocumentRepository
import org.koin.dsl.module

val legalModule = module {
  single<LegalDocumentRepository> { FirebaseLegalDocumentRepository(get()) }
}
