package com.valorant.devopshub.data;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.valorant.devopshub.model.Weapon;

@Repository
public class WeaponRepository {

    private final List<Weapon> weapons = List.of(
            new Weapon("classic", "Classic", "Sidearm", 0, "26-78", "6.75 rps", 12),
            new Weapon("shorty", "Shorty", "Sidearm", 150, "12-140", "3.3 rps", 2),
            new Weapon("frenzy", "Frenzy", "Sidearm", 450, "26-78", "10 rps", 13),
            new Weapon("ghost", "Ghost", "Sidearm", 500, "30-105", "6.75 rps", 15),
            new Weapon("sheriff", "Sheriff", "Sidearm", 800, "55-159", "4 rps", 6),
            new Weapon("stinger", "Stinger", "SMG", 1100, "27-67", "16 rps", 20),
            new Weapon("spectre", "Spectre", "SMG", 1600, "26-78", "13.33 rps", 30),
            new Weapon("bucky", "Bucky", "Shotgun", 850, "20-146", "1.1 rps", 5),
            new Weapon("judge", "Judge", "Shotgun", 1850, "17-159", "3.5 rps", 7),
            new Weapon("bulldog", "Bulldog", "Rifle", 2050, "35-105", "9.15 rps", 24),
            new Weapon("guardian", "Guardian", "Rifle", 2250, "65-195", "6.75 rps", 12),
            new Weapon("phantom", "Phantom", "Rifle", 2900, "39-156", "11 rps", 30),
            new Weapon("vandal", "Vandal", "Rifle", 2900, "40-160", "9.75 rps", 25),
            new Weapon("operator", "Operator", "Sniper Rifle", 4700, "150-255", "0.6 rps", 5)
    );

    public List<Weapon> findAll() {
        return weapons;
    }
}
