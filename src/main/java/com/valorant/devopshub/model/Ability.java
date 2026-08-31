package com.valorant.devopshub.model;

/**
 * A single agent ability. "type" is one of: Basic, Signature, Ultimate,
 * matching how VALORANT itself categorizes abilities.
 */
public record Ability(String name, String type, String description) {
}
