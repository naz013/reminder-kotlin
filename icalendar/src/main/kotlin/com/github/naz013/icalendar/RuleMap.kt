package com.github.naz013.icalendar

data class RuleMap(
  val map: Map<TagType, Tag>
) {

  inline fun <reified T : Tag> getTagOrNull(tagType: TagType): T? {
    return if (hasTag(tagType)) {
      map[tagType]?.takeIf { it is T }?.let { it as? T }
    } else {
      null
    }
  }

  fun hasTag(tagType: TagType): Boolean {
    return map.containsKey(tagType)
  }
}
