package com.valorant.devopshub.model;

import java.util.List;

/**
 * A map profile shown on the /maps page. Like agents, the "image" is a
 * generated gradient tile (accentColor) rather than real map artwork.
 */
public record GameMap(
        String id,
        String name,
        String type,
        String description,
        List<String> sites,
        String accentColor
) {
}
