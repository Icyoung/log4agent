// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "Log4Agent",
    platforms: [
        .iOS(.v13),
        .macOS(.v12)
    ],
    products: [
        .library(name: "Log4Agent", targets: ["Log4Agent"])
    ],
    targets: [
        .target(
            name: "Log4Agent",
            path: "ios/Sources/Log4Agent"
        ),
        .testTarget(
            name: "Log4AgentTests",
            dependencies: ["Log4Agent"],
            path: "ios/Tests/Log4AgentTests"
        )
    ]
)

