package com.valorant.devopshub.model;

import java.util.List;

/**
 * An agent profile shown on the /agents page.
 *
 * Note on "image": to avoid using or copying Riot Games artwork, agents are
 * rendered in the UI as a generated avatar (a colored badge with initials)
 * instead of real character art. avatarColor + avatarInitials are the data
 * that drives that placeholder graphic.
 */
public record Agent(
        String id,
        String name,
        String role,
        String region,
        String difficulty,
        String description,
        List<Ability> abilities,
        String avatarColor,
        String avatarInitials
) {
}
