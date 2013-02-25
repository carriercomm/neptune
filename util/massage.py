import glob
import json


def generate_data():
    iter = glob.glob('./zones/*.json')

    # global neptune namespace
    g_namespace = 'org.jarsonmar.neptune'

    out_data = {}
    combined = {"loc": {}, "obj": {}, "mob": {}}
    for path in iter:
        d = json.loads(open(path, "r").read())
        for key in ["loc", "obj", "mob"]:
            combined[key] = dict(combined[key].items() + d[key].items())

    locations = combined["loc"]
    for loc in locations.iteritems():
        loc_id, loc_data, = loc
        local_id, zone = loc_id.split('@')

        ns = lambda key: "%s:loc:%s:%s:%s" % (g_namespace, zone, local_id, key)

        if ns("properties") not in out_data:
            out_data[ns("properties")] = {}

        out_data[ns("properties")]["title"]       = loc_data["title"]
        out_data[ns("properties")]["description"] = loc_data["description"]
        out_data[ns("properties")]["altitude"]    = loc_data["altitude"]

        out_data[ns("flags")] = loc_data["flags"]

        for direction in ["n", "s", "e", "w", "u", "d"]:
            if direction not in loc_data["exits"]:
                continue

            ex = loc_data["exits"][direction]
            if "@" not in ex:
                ex += "@" + zone

            if ex.startswith("^"):
                ex = ex.lstrip("^")
                object_link_key = "%s:%s:%s:%s:links" % (g_namespace, "obj", zone, local_id)
                if object_link_key not in out_data:
                    out_data[object_link_key] = {}
                out_data[object_link_key][direction] = ex
            else:
                a = ex.split("@") # TODO use join.reveerse etc
                exit_value = "%s:%s" % (a[1], a[0])
                if ns("exits") not in out_data:
                    out_data[ns("exits")] = {}
                out_data[ns("exits")][direction] = exit_value

    objects = combined["obj"]
    for obj in objects.iteritems():
        obj_id, obj_data, = obj
        local_id, zone = obj_id.split('@')

        ns = lambda key: "%s:obj:%s:%s:%s" % (g_namespace, zone, local_id, key)

        if ns("properties") not in out_data:
            out_data[ns("properties")] = {}
        for prop in ["visibility", "state", "examine[0]", "weight", "bvalue",
                    "desc[3]", "name", "desc[2]", "pname",
                    "examine[1]", "desc[0]", "damage", "examine", "size",
                    "desc[]", "maxstate", "desc[1]", "altname", "armor", "linked"]:
            if prop in obj_data:
                out_data[ns("properties")][prop] = obj_data[prop]

        if "oflag" in obj_data:
            out_data[ns("oflags")] = obj_data["oflag"]
        for flags in ["oflags", "aflags"]:
            if flags in obj_data:
                out_data[ns(flags)] = obj_data[flags]

        if "location" in obj_data:
            if ":" not in obj_data["location"]:
                obj_data["location"] += ":" + zone

            loc_type, dest = obj_data["location"].split(":")
            if "@" not in dest:
                dest += "@" + zone

            # ThInG@zone becomes zone:thing
            a = dest.split("@")
            a.reverse()
            dest_value = ':'.join(a).lower()

            # These will be in redis sets
            if loc_type == "IN_ROOM":
                room_key = ":".join([g_namespace, "loc", dest_value, "objs"])
                if room_key not in out_data:
                    out_data[room_key] = []
                out_data[room_key].append(':'.join([zone, local_id]))
                out_data[ns("location")] = dest_value
            elif loc_type == "IN_CONTAINER":
                cont_key = ":".join([g_namespace, "obj", dest_value, "containing"])
                if cont_key not in out_data:
                    out_data[cont_key] = []
                out_data[cont_key].append(':'.join([zone, local_id]))
                out_data[ns("container")] = dest_value
            elif loc_type == "WIELDED_BY":
                wield_key = ":".join([g_namespace, "mob", dest_value, "wielding"])
                out_data[wield_key] = ':'.join([zone, local_id]) # only one at a time
                out_data[ns("wieldedby")] = dest_value
            elif loc_type == "WORN_BY":
                wear_key = ":".join([g_namespace, "mob", dest_value, "wearing"])
                if wear_key not in out_data:
                    out_data[wear_key] = []
                out_data[wear_key].append(':'.join([zone, local_id]))
                out_data[ns("wornby")] = dest_value
            elif loc_type == "CARRIED_BY":
                carry_key = ":".join([g_namespace, "mob", dest_value, "carrying"])
                if carry_key not in out_data:
                    out_data[carry_key] = []
                out_data[carry_key].append(':'.join([zone, local_id]))
                out_data[ns("carriedby")] = dest_value

    mobiles = combined["mob"]
    for mob in mobiles.iteritems():
        mob_id, mob_data, = mob
        local_id, zone = mob_id.split('@')

        ns = lambda key: "%s:mob:%s:%s:%s" % (g_namespace, zone, local_id, key)

        if ns("properties") not in out_data:
            out_data[ns("properties")] = {}
        for prop in ["visibility", "damage", "examine", "strength", "desc", "speed",
                    "aggression", "location", "name", "armor", "description",
                    "wimpy", "pname"]:
            if prop in mob_data:
                out_data[ns("properties")][prop] = mob_data[prop]
                if prop == "location":
                    dest = mob_data["location"]

                    if "@" not in dest:
                        dest += "@" + zone

                    a = dest.split("@")
                    a.reverse()
                    dest_value = ':'.join(a).lower()

                    room_key = ":".join([g_namespace, "loc", dest_value, "mobs"])
                    if room_key not in out_data:
                        out_data[room_key] = []
                    out_data[room_key].append(':'.join([zone, local_id]))
                    out_data[ns("location")] = dest_value

        for flags in ["eflags", "sflag", "sflags",
                    "pflag", "pflags", "mflag", "mflags"]:
            if prop in mob_data:
                flag_key = prop
                if flag_key.endswith("g"):
                    flag_key += "s"
                out_data[ns("properties")][flag_key] = mob_data[prop]
    return out_data

f = open("new.json", "w")
f.write(json.dumps(generate_data(), indent=2))
f.close()
