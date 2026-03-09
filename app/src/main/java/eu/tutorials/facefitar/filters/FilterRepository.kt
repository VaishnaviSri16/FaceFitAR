package eu.tutorials.facefitar.filters

import eu.tutorials.facefitar.R

object FilterRepository {

    val filters = listOf(
        FilterModel(0, "Original", R.drawable.ic_original, FilterType.ORIGINAL),
        FilterModel(1, "Rose Crown", R.drawable.ic_floral_crown, FilterType.CROWN),
        FilterModel(2, "Crown", R.drawable.ic_crown, FilterType.CROWN),
        FilterModel(3, "Heart Crown", R.drawable.ic_heart_crown, FilterType.CROWN),
        FilterModel(4, "Bunny", R.drawable.ic_bunny, FilterType.EARS),
        FilterModel(5, "Glasses", R.drawable.ic_glasses, FilterType.GLASSES),
        FilterModel(6, "Cat Ears", R.drawable.ic_animal_ears, FilterType.EARS),
        FilterModel(7, "Dog Ears", R.drawable.ic_dog_ears, FilterType.EARS),
        FilterModel(8, "Black Mask", R.drawable.ic_black_mask, FilterType.MASK),
        FilterModel(10, "Mystic Mask", R.drawable.ic_mask, FilterType.MASK),
        FilterModel(11, "Sparkle", R.drawable.ic_sparkle, FilterType.SPARKLE)
    )
}
