package com.valorant.devopshub.data;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.valorant.devopshub.model.Ability;
import com.valorant.devopshub.model.Agent;

/**
 * In-memory agent catalog. There is deliberately no database in this
 * project - the DevOps lab is about build/deploy/operate, not persistence,
 * so a simple immutable in-memory list keeps the application easy to
 * reason about at every layer (Docker, Jenkins, systemd, etc.).
 */
@Repository
public class AgentRepository {

    private final List<Agent> agents = List.of(
            new Agent("jett", "Jett", "Duelist", "South Korea", "Easy",
                    "A fast, evasive duelist who darts around the battlefield, using her mobility to " +
                            "get in, get a pick, and get out before the enemy team can react.",
                    List.of(
                            new Ability("Cloudburst", "Basic", "Throws a fast-moving cloud of fog that obscures vision on impact."),
                            new Ability("Updraft", "Basic", "Propels Jett straight up into the air."),
                            new Ability("Tailwind", "Signature", "Jett briefly moves at incredible speed, including through the air."),
                            new Ability("Blade Storm", "Ultimate", "Equips a set of highly accurate throwing knives that deal massive damage.")
                    ),
                    "#7CE0D3", "JT"),

            new Agent("phoenix", "Phoenix", "Duelist", "United Kingdom", "Easy",
                    "A self-sufficient duelist who wraps himself in fire to fuel his own regeneration " +
                            "and push aggressively onto sites.",
                    List.of(
                            new Ability("Blaze", "Basic", "Draws a wall of fire that blocks vision and damages enemies passing through."),
                            new Ability("Curveball", "Basic", "Flings a curving flare that blinds enemies who look at it."),
                            new Ability("Hot Hands", "Signature", "Throws a fireball that heals Phoenix if he stands in it."),
                            new Ability("Run it Back", "Ultimate", "Marks a spot, granting a second life for a short duration.")
                    ),
                    "#FF6B4A", "PH"),

            new Agent("sage", "Sage", "Sentinel", "China", "Easy",
                    "A calm, supportive sentinel who can heal teammates, resurrect a fallen ally, and " +
                            "slow down enemy pushes with walls and slow fields.",
                    List.of(
                            new Ability("Slow Orb", "Basic", "Creates a slowing field that also breaks stealth."),
                            new Ability("Healing Orb", "Signature", "Heals a teammate or Sage herself over time."),
                            new Ability("Barrier Orb", "Basic", "Creates a solid wall to block enemy movement and sightlines."),
                            new Ability("Resurrection", "Ultimate", "Revives a fallen teammate with full health.")
                    ),
                    "#8FD9E8", "SG"),

            new Agent("sova", "Sova", "Initiator", "Russia", "Medium",
                    "A recon-focused initiator who fires tracking darts and drones to reveal enemy " +
                            "positions before his team pushes in.",
                    List.of(
                            new Ability("Owl Drone", "Basic", "A flyable drone that can tag enemies it spots."),
                            new Ability("Shock Bolt", "Basic", "A bolt that explodes into a damaging burst of static energy."),
                            new Ability("Recon Bolt", "Signature", "A bolt that reveals the location of nearby enemies on hit."),
                            new Ability("Hunter's Fury", "Ultimate", "Fires energy blasts that pierce through walls and damage enemies.")
                    ),
                    "#5C7CBF", "SV"),

            new Agent("omen", "Omen", "Controller", "Unknown", "Medium",
                    "A shadow-formed controller who blinds enemies and teleports short distances to " +
                            "control vision across the map.",
                    List.of(
                            new Ability("Shrouded Step", "Basic", "Teleports Omen a short distance forward after a brief delay."),
                            new Ability("Paranoia", "Basic", "Sends out a shadow projectile that nearsights enemies it hits."),
                            new Ability("Dark Cover", "Signature", "Fires a shadow orb that pops into a smoke cloud at range."),
                            new Ability("From the Shadows", "Ultimate", "Teleports Omen anywhere on the map, briefly becoming a shadow.")
                    ),
                    "#4A4560", "OM"),

            new Agent("reyna", "Reyna", "Duelist", "Mexico", "Medium",
                    "A high-risk, high-reward duelist who feeds on the souls of enemies she defeats to " +
                            "heal or turn invisible.",
                    List.of(
                            new Ability("Leer", "Basic", "Summons an ethereal eye that nearsights anyone who looks at it."),
                            new Ability("Devour", "Signature", "Consumes a soul to heal herself for a large amount."),
                            new Ability("Dismiss", "Signature", "Consumes a soul to become intangible and reposition."),
                            new Ability("Empress", "Ultimate", "Enters a frenzy, gaining combat speed and attack rate.")
                    ),
                    "#7A2E5C", "RY"),

            new Agent("killjoy", "Killjoy", "Sentinel", "Germany", "Hard",
                    "A gadgeteer sentinel who locks down areas with turrets, alarm bots and a nanoswarm " +
                            "grenade to hold sites and watch flanks.",
                    List.of(
                            new Ability("Nanoswarm", "Basic", "Deploys a hidden grenade that can be detonated to damage enemies."),
                            new Ability("Alarmbot", "Basic", "Deploys a bot that leaps at nearby enemies and marks them as vulnerable."),
                            new Ability("Turret", "Signature", "Deploys a turret that fires at enemies in its cone of fire."),
                            new Ability("Lockdown", "Ultimate", "Deploys a device that detains all enemies caught in its radius.")
                    ),
                    "#F2D94E", "KJ"),

            new Agent("cypher", "Cypher", "Sentinel", "Morocco", "Medium",
                    "An information-gathering sentinel who sets tripwires and cameras to watch flanks " +
                            "and reveal the enemy team's position.",
                    List.of(
                            new Ability("Trapwire", "Basic", "A tripwire that tethers and reveals enemies who trigger it."),
                            new Ability("Cyber Cage", "Basic", "Throws a cage that blocks vision when activated."),
                            new Ability("Spycam", "Signature", "Places a remote camera that can tag enemies from a distance."),
                            new Ability("Neural Theft", "Ultimate", "Extracts data from an eliminated enemy to reveal the whole team.")
                    ),
                    "#C7B37A", "CY"),

            new Agent("breach", "Breach", "Initiator", "Sweden", "Medium",
                    "A fearless initiator who fires blasts through walls to stun, flash and disorient " +
                            "enemies ahead of a push.",
                    List.of(
                            new Ability("Aftershock", "Basic", "Fires a charge that erupts in a damaging fault line through walls."),
                            new Ability("Flashpoint", "Basic", "Fires a flash charge through walls that blinds on activation."),
                            new Ability("Fault Line", "Signature", "Fires a shockwave that stuns enemies in a line."),
                            new Ability("Rolling Thunder", "Ultimate", "Charges up a series of quakes that stun and knock back enemies.")
                    ),
                    "#B5651D", "BR"),

            new Agent("raze", "Raze", "Duelist", "Brazil", "Easy",
                    "An explosives specialist who clears corners and space with grenades, satchels and " +
                            "a rocket launcher ultimate.",
                    List.of(
                            new Ability("Boom Bot", "Basic", "Deploys a rolling bot that chases nearby enemies and explodes."),
                            new Ability("Blast Pack", "Signature", "A satchel that damages and knocks back enemies, or can be used for mobility."),
                            new Ability("Paint Shells", "Basic", "Throws a grenade that bursts into a cluster of small explosions."),
                            new Ability("Showstopper", "Ultimate", "Fires a rocket launcher that deals heavy damage in a large area.")
                    ),
                    "#E0742A", "RZ"),

            new Agent("viper", "Viper", "Controller", "United States", "Hard",
                    "A chemical-warfare controller who uses toxic gas and a poisonous wall to zone out " +
                            "and weaken enemies over time.",
                    List.of(
                            new Ability("Poison Cloud", "Basic", "Throws a gas emitter that can be reactivated to create a toxic cloud."),
                            new Ability("Toxic Screen", "Signature", "Deploys a long line of gas emitters forming a tall toxic wall."),
                            new Ability("Snake Bite", "Basic", "Fires a canister that creates a decaying pool of toxins."),
                            new Ability("Viper's Pit", "Ultimate", "Deploys a massive toxic cloud that reduces enemy vision and max health.")
                    ),
                    "#3E8E5A", "VP"),

            new Agent("brimstone", "Brimstone", "Controller", "United States", "Easy",
                    "A no-nonsense controller who calls in orbital strikes and smokes from a tactical " +
                            "map to support his team's execute.",
                    List.of(
                            new Ability("Stim Beacon", "Basic", "Deploys a beacon that grants increased fire rate and equip speed."),
                            new Ability("Incendiary", "Basic", "Launches a grenade that creates a damaging field of fire."),
                            new Ability("Sky Smoke", "Signature", "Uses a tactical map to call in smokes on precise locations."),
                            new Ability("Orbital Strike", "Ultimate", "Calls in a devastating laser that deals heavy damage over time.")
                    ),
                    "#A5471C", "BM")
    );

    public List<Agent> findAll() {
        return agents;
    }
}
