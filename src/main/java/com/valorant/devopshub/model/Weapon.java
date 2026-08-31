package com.valorant.devopshub.model;

/**
 * A weapon profile shown on the /weapons page.
 */
public record Weapon(
        String id,
        String name,
        String category,
        int price,
        String damage,
        String fireRate,
        int magazineSize
) {
}
