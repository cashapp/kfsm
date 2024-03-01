# Change Log

## [Unreleased]

## [0.1.0]

### Breaking

* `StateMachine.verify` no longer requires a second argument to declare the base type of the state machine. This is now
  inferred from the first argument.`

### Added

* `StateMachine.mermaid` is a new utility that will generate mermaid diagram from your state machine.
* New simplified API is being introduced to make it easier to use the library. This can be found in the package 
  `app.cash.kfsm`. It is not yet ready for production use, but we are looking for feedback on the new API.


## [0.0.2] - 2023-09-11

### Added

* Initial release from internal

