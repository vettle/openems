export enum WidgetNature {
    'io.openems.edge.evcs.api.Evcs',
    'io.openems.impl.controller.channelthreshold.ChannelThresholdController', // TODO deprecated
    'io.openems.impl.controller.heatingelement.HeatingElement'
}

export enum WidgetFactory {
    'Controller.Api.ModbusTcp',
    'Controller.ChannelThreshold',
    'Controller.Heatingelement'
}

export class Widget {
    name: WidgetNature | WidgetFactory;
    componentId: string
}