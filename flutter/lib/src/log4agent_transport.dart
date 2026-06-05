import 'package:dio/dio.dart';
import 'package:http/http.dart' as http;

abstract interface class Log4AgentTransport {
  Future<void> postJson(String url, String jsonBody);

  void close() {}
}

class Log4AgentHttpTransport implements Log4AgentTransport {
  Log4AgentHttpTransport([http.Client? client])
      : _client = client ?? http.Client(),
        _ownsClient = client == null;

  final http.Client _client;
  final bool _ownsClient;

  @override
  Future<void> postJson(String url, String jsonBody) async {
    await _client.post(
      Uri.parse(url),
      headers: {'content-type': 'application/json'},
      body: jsonBody,
    );
  }

  @override
  void close() {
    if (_ownsClient) _client.close();
  }
}

class Log4AgentDioTransport implements Log4AgentTransport {
  Log4AgentDioTransport(this._dio);

  final Dio _dio;

  @override
  Future<void> postJson(String url, String jsonBody) async {
    await _dio.postUri(
      Uri.parse(url),
      data: jsonBody,
      options: Options(
        contentType: Headers.jsonContentType,
        responseType: ResponseType.plain,
      ),
    );
  }

  @override
  void close() {}
}
