namespace java org.jarsonmar.neptune.thrift

enum LocProp {
    MOBS = 1,
    PLRS = 2,
    OBJS = 3,
    EXIT = 4,
    PROP = 5,
    FLAG = 6,
}

enum ExitProp {
    ALL   = 1,
    NORTH = 2,
    SOUTH = 3,
    EAST  = 4,
    WEST  = 5,
    UP    = 6,
    DOWN  = 7,
}

struct LocReadRequest {
    1: set<string>    id,
    3: map<LocProp, set<string>> props,
}

struct LocReadInstance {
    1: string id,
    2: set<string> mobs,
    3: set<string> plrs,
    4: set<string> objs,
    5: map<ExitProp, string> exits,
    6: map<string, string> props,
    7: set<string> flags,
}

struct LocReadResponse {
    1: map<string, LocReadInstance> locs,
}

struct MobileMovement {
    1: string mob,
    2: string locFrom,
    3: string locTo,
    4: ExitProp direction,
}

service ControllerUpdateService {
    LocReadResponse readRequest(1: LocReadRequest req),
}

service NatureUpdateService {
    bool mobileMovement(1: MobileMovement req),
}
