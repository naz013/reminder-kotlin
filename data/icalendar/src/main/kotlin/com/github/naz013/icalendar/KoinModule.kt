package com.github.naz013.icalendar

import com.github.naz013.icalendar.builder.RuleBuilder
import com.github.naz013.icalendar.parser.TagParser
import org.koin.dsl.module

val iCalendarModule = module {
  factory { RuleBuilder() }
  factory { TagParser() }

  factory { ICalendarApiImpl(get(), get()) as ICalendarApi }
}
