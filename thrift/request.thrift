namespace java org.jarsonmar.neptune.thrift

enum RequestType {
    CREATE = 1,
    READ   = 2,
    UPDATE = 3,
    DELETE = 4,
    LIST   = 5,
}

struct Request {
    1: RequestType rtype,
    2: map<string, string> params, // XXX fill out with specific data
}

service RequestService {
    void processRequest(1: Request req),
}
