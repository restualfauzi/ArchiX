package com.archix.app.data.model

enum class SortType(val titleId: String, val titleEn: String) {
    NAME("Nama", "Name"),
    SIZE("Ukuran", "Size"),
    DATE("Tanggal", "Date"),
    TYPE("Jenis", "Type")
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

enum class ViewMode {
    LIST,
    GRID
}
