import { Component, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ChannelAddress, Edge, EdgeConfig, Service, Websocket } from '../../../../shared/shared';

@Component({
  selector: 'channelthreshold',
  templateUrl: './channelthreshold.component.html'
})
export class ChannelthresholdComponent {

  private static readonly SELECTOR = "channelthreshold";

  @Input() private componentId: string;

  public edge: Edge = null;
  public controller: EdgeConfig.Component = null;
  public outputChannel: ChannelAddress = null;

  constructor(
    private service: Service,
    private websocket: Websocket,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    // Subscribe to CurrentData
    this.service.setCurrentComponent('', this.route).then(edge => {
      this.edge = edge;

      edge.subscribeChannels(this.websocket, ChannelthresholdComponent.SELECTOR + this.componentId, [

        new ChannelAddress(this.componentId, 'Status'),
        new ChannelAddress(this.componentId, 'L1_activation'),
        new ChannelAddress(this.componentId, 'L2_activation'),
        new ChannelAddress(this.componentId, 'L3_activation'),
        new ChannelAddress(this.componentId, 'Hysteresis')
      ]);

      this.service.getConfig().then(config => {
        this.outputChannel = ChannelAddress.fromString(config.getComponentProperties(this.componentId)['outputChannelAddress']);
      });
    });
  }

  ngOnDestroy() {
    if (this.edge != null) {
      this.edge.unsubscribeChannels(this.websocket, ChannelthresholdComponent.SELECTOR + this.componentId);
    }
  }

  currentPower(s: Status): number {
    switch (s) {
      case Status.OFF:
        return 0;
      case Status.L1:
        return 2;
      case Status.L2:
        return 4;
      case Status.L3:
        return 6;
      default:
        return 0;
    }
  }
}

enum Status {
  OFF = -1, // Value is smaller than threshold1.
  L1,       // Value is larger threshold1 but smaller than threshold2.
  L2,       // Value is larger threshold2 but smaller than threshold3.
  L3        // Value is larger threshold3.
}